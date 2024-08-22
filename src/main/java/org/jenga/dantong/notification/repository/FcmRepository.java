package org.jenga.dantong.notification.repository;

import lombok.RequiredArgsConstructor;
import org.jenga.dantong.notification.model.dto.request.TokenRegisterRequest;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class FcmRepository {

    private final StringRedisTemplate tokenRedisTemplate;
    private final UserRepository userRepository;

    public void saveToken(TokenRegisterRequest request) {
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

    public List<String> getAllTokens() {

        return userRepository.findAll()
                .stream()
                .map(user -> {
                    return tokenRedisTemplate.opsForValue().get(user.getStudentId());
                })
                .filter(Objects::nonNull)
                .toList();
    }
}