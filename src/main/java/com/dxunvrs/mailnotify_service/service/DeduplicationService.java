package com.dxunvrs.mailnotify_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DeduplicationService {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "mail_notify:msg_id";

    public boolean isNewMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return true;
        }

        String key = KEY_PREFIX + messageId;
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(key, "processed", Duration.ofDays(1));

        return Boolean.TRUE.equals(isAbsent);
    }
}