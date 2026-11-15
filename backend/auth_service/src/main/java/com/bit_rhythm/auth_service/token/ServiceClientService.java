package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.admin.dto.request.ServiceClientRegisterRequest;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.util.ScopeUtils;
import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.PermissionRepository;
import com.bit_rhythm.auth_service.token.dto.response.CreatedClient;
import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.core.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ServiceClientService {
    private static final int SECRET_BYTES = 32;

    private final ServiceClientRepository serviceClientRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreatedClient register(ServiceClientRegisterRequest request) {
        if (serviceClientRepository.existsByClientIdAndIsDeleted(request.clientId(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(AuthCode.CLIENT_ALREADY_EXISTS);
        }

        String secret = generateSecret();
        String secretHash = passwordEncoder.encode(secret);

        Set<Permission> permissions = permissionRepository.findAllByNameInIgnoreCaseAndIsDeleted(request.scopes(), CommonEnums.DeletedStatus.NOT_DELETED.getCode());

        ServiceClient client = ServiceClient.builder()
                .clientId(request.clientId())
                .clientSecretHash(secretHash)
                .scopes(permissions)
                .allowedAudiences(ScopeUtils.join(request.audiences()))
                .isActive(true)
                .build();

        serviceClientRepository.save(client);
        return new CreatedClient(request.clientId(), secret);
    }

    public List<ServiceClient> searchServiceClients() {
        return serviceClientRepository.findAllByIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
    }

    @Transactional
    public String rotateSecret(String clientId) {
        ServiceClient client = findOrThrow(clientId);
        String newSecret = generateSecret();
        client.setClientSecretHash(passwordEncoder.encode(newSecret));
        serviceClientRepository.save(client);
        return newSecret;
    }

    @Transactional
    public void deactivate(String clientId) {
        ServiceClient client = findOrThrow(clientId);
        client.setIsActive(false);
        client.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        serviceClientRepository.save(client);
    }

    private ServiceClient findOrThrow(String clientId) {
        return serviceClientRepository
                .findByClientIdAndIsDeleted(clientId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Service client not found: " + clientId));
    }

    private String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
