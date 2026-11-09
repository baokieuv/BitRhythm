package com.bit_rhythm.notification_service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications/admin/dlq")
@RequiredArgsConstructor
public class AdminDlqController {

    private final DlqReplayService dlqReplayService;

    @PostMapping("/replay")
    public Map<String, Integer> replay(@RequestParam(defaultValue = "100") int max) {
        return Map.of("replayed", dlqReplayService.replay(Math.min(max, 1000)));
    }
}
