package org.jenga.dantong.notification.repository;

import lombok.RequiredArgsConstructor;
import org.jenga.dantong.notification.model.dto.request.NotificationRequest;
import org.jenga.dantong.user.model.dto.request.LoginRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FcmRepository {

    private final StringRedisTemplate tokenRedisTemplate;

    public void saveToken(NotificationRequest request) {
        tokenRedisTemplate.opsForValue()
                .set(request.getStudentId(), request.getToken());
    }

    public String getToken(String studentId) {
        return tokenRedisTemplate.opsForValue().get(studentId);
    }

    public void deleteToken(String studentId) {
        tokenRedisTemplate.delete(studentId);
    }

    public boolean hasKey(String studentId) {
        return tokenRedisTemplate.hasKey(studentId);
    }
}