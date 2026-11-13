package com.bit_rhythm.user_service.lifecycle;

import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockRepository;
import com.bit_rhythm.user_service.common.enums.UserEventType;
import com.bit_rhythm.user_service.follow.Follow;
import com.bit_rhythm.user_service.follow.FollowRepository;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;

import static com.bit_rhythm.user_service.common.enums.UserEventType.*;

/** [S2] Keeps this service in step with the accounts of auth-service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLifecycleService {

    private static final Set<String> HANDLED = Set.of(USER_REGISTERED.getName(), USER_DISABLED.getName(), USER_DELETED.getName());

    private static final String DELETED_DISPLAY_NAME = "Deleted user";
    private static final int MAX_DISPLAY_NAME = 100;

    private final ProcessedEventRepository processedEventRepository;
    private final ProfileRepository profileRepository;
    private final SettingsRepository settingsRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final StorageClient storageClient;

    /**
     * ONE transaction per event. The processed marker is inserted FIRST, in the same transaction as the work: if the work
     * fails, the marker is rolled back with it and the retry starts again; if the event is delivered twice, the second
     * delivery finds the marker and stops. Unknown event types are ignored without a marker (nothing to remember).
     */
    @Transactional
    public void handle(AuthEvent event) {
        if (!HANDLED.contains(event.eventType())) {
            log.debug("Ignoring auth event type {}", event.eventType());
            return;
        }
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Auth event {} already handled, skipping", event.eventId());
            return;
        }
        processedEventRepository.saveAndFlush(new ProcessedEvent(event.eventId()));

        switch (UserEventType.fromName(event.eventType())) {
            case USER_REGISTERED -> onUserRegistered(event);
            case USER_DISABLED -> onUserDisabled(event.userId());
            case USER_DELETED -> onUserDeleted(event.userId());
            case null -> { }
            default -> { }   // unreachable: HANDLED was checked above
        }
    }

    /**
     * A user was created in auth-service: create the profile and the default settings, so the user can be followed and found
     * before ever calling this service. Idempotent: if the user called the API first, ProfileService.getOrCreate already made
     * the profile and only the missing parts are added. If both happen at the same instant the insert fails on the primary key,
     * the transaction rolls back and Kafka retries the event, which then finds the profile.
     */
    public void onUserRegistered(AuthEvent event) {
        Long userId = event.userId();
        if (!profileRepository.existsById(userId)) {
            profileRepository.saveAndFlush(new UserProfile(userId, displayNameOf(event), event.email()));
        }
        if (!settingsRepository.existsById(userId)) {
            settingsRepository.save(new UserSettings(userId));
        }
    }

    /** Disabled accounts are hidden, not erased: status HIDDEN. A missing profile (never created) or a deleted one is left alone. */
    public void onUserDisabled(Long userId) {
        profileRepository.findById(userId)
                .filter(profile -> profile.getStatus() == ProfileStatus.ACTIVE)
                .ifPresent(profile -> {
                    profile.setStatus(ProfileStatus.HIDDEN);
                    profileRepository.save(profile);
                });
    }

    /**
     * Account deleted: anonymize the profile, remove the follows and blocks, delete the settings and, AFTER the commit, the avatar
     * object. The counters of the OTHER users are corrected first (once the rows are gone nobody could tell whom to correct).
     */
    public void onUserDeleted(Long userId) {
        for (Follow follow : followRepository.findByFollowerId(userId)) {        // the user followed them
            profileRepository.addFollowers(follow.getFolloweeId(), -1);
        }
        for (Follow follow : followRepository.findByFolloweeId(userId)) {        // they followed the user
            profileRepository.addFollowing(follow.getFollowerId(), -1);
        }
        followRepository.deleteAllOf(userId);
        blockRepository.deleteAllOf(userId);

        // read the profile AFTER the bulk updates above (they clear the persistence context)
        profileRepository.findById(userId).ifPresent(profile -> {
            String avatarKey = profile.getAvatarKey();
            profile.setStatus(ProfileStatus.DELETED);
            profile.setDisplayName(DELETED_DISPLAY_NAME);
            profile.setBio(null);
            profile.setCountry(null);
            profile.setAvatarKey(null);
            profile.setVerified(false);
            profile.setFollowerCount(0);
            profile.setFollowingCount(0);
            profileRepository.save(profile);
            deleteAvatarAfterCommit(avatarKey);
        });
        settingsRepository.deleteById(userId);
    }

    // ------------------------------------------------------------------ internals

    /** The username chosen at registration; otherwise the part of the email before "@"; otherwise "user{id}". */
    private static String displayNameOf(AuthEvent event) {
        String name = event.username() == null ? "" : event.username().trim();
        if (name.isEmpty() && event.email() != null) {
            int at = event.email().indexOf('@');
            name = (at > 0 ? event.email().substring(0, at) : event.email()).trim();
        }
        if (name.isEmpty()) {
            return "user" + event.userId();
        }
        return name.length() <= MAX_DISPLAY_NAME ? name : name.substring(0, MAX_DISPLAY_NAME);
    }

    /** The object store is not transactional: delete only once the database change is committed, and never fail the event over it. */
    private void deleteAvatarAfterCommit(String key) {
        if (key == null) {
            return;
        }
        Runnable delete = () -> {
            try {
                storageClient.delete(key);
            } catch (RuntimeException e) {
                log.warn("Could not delete avatar object {}: {}", key, e.toString());   // an orphan file is harmless
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete.run();
                }
            });
        } else {
            delete.run();
        }
    }
}