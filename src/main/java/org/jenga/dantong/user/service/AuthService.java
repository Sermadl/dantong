package org.jenga.dantong.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jenga.dantong.user.exception.AlreadyStudentIdException;
import org.jenga.dantong.user.model.DkuAuth;
import org.jenga.dantong.user.model.dto.UserInfo;
import org.jenga.dantong.user.model.dto.request.DkuInfoRequest;
import org.jenga.dantong.user.model.dto.response.StudentInfo;
import org.jenga.dantong.user.model.dto.response.VerifiedStudentResponse;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.SignupRedisRepository;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String DKU_AUTH_NAME = "dku";

    private final Clock clock;
    private final UserRepository userRepository;
    private final SignupRedisRepository redisRepository;
    private final DkuAuthService dkuAuthService;

    private final DkuCrawlService dkuCrawlService;

    private final SignupRedisRepository signupRedisRepository;


    public void getStudentInfo(String signupToken) {
        Instant now = Instant.now(clock);
        redisRepository.getAuthPayload(signupToken, DKU_AUTH_NAME, UserInfo.class, now);
    }

    public boolean deleteStudentAuth(String signupToken) {
        return redisRepository.deleteAuthPayload(signupToken, DKU_AUTH_NAME);
    }

    public VerifiedStudentResponse verifyDkuInfo(DkuInfoRequest request) {
        String signupToken = UUID.randomUUID().toString();
        checkAlreadyStudentId(request.getDkuId());

        StudentInfo info = getDkuInfo(request.getDkuId(), request.getDkuPassword());
        signupRedisRepository.setAuthPayload(signupToken, DKU_AUTH_NAME, info, Instant.now(clock));

        return new VerifiedStudentResponse(signupToken, info);

    }

    public StudentInfo getDkuInfo(String id, String password) {
        DkuAuth auth = dkuAuthService.loginWebInfo(id, password);

        StudentInfo studentInfo = dkuCrawlService.crawlStudentInfo(auth);
        return studentInfo;
    }

    private void checkAlreadyStudentId(String studentId) {
        Optional<User> alreadyUser = userRepository.findByStudentId(studentId);
        if (alreadyUser.isPresent()) {
            throw new AlreadyStudentIdException();
        }
    }
}
