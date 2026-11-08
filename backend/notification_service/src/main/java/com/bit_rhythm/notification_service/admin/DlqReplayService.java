package com.bit_rhythm.notification_service.admin;

import org.springframework.stereotype.Service;

@Service
public class DlqReplayService {

    public int replay(int maxMessages) {
        throw new UnsupportedOperationException("TODO: DLT replay");
    }
}