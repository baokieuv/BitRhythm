package com.bit_rhythm.content_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Play-count flush, trending refresh, history retention, outbox worker. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
