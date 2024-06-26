package org.jenga.dantong.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.global.util.CodeGenerator;
import org.jenga.dantong.global.util.TextTemplateEngine;
import org.jenga.dantong.infra.nhn.service.NHNEmailService;
import org.jenga.dantong.user.exception.AlreadyStudentIdException;
import org.jenga.dantong.user.exception.EmailCodeNotMatchException;
import org.jenga.dantong.user.exception.InvalidatedTokenException;
import org.jenga.dantong.user.model.dto.UserInfo;
import org.jenga.dantong.user.model.dto.request.EmailRequest;
import org.jenga.dantong.user.model.dto.request.EmailVerifyRequest;
import org.jenga.dantong.user.model.dto.response.StudentVerifyResponse;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.SignupRedisRepository;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String DKU_AUTH_NAME = "dku";
    private static final String EMAIL_AUTH_NAME = "email";

    private final Clock clock;

    private final UserRepository userRepository;
    private final SignupRedisRepository signupRedisRepository;
    private final NHNEmailService nhnEmailService;

    @Value("${app.auth.email.code-length}")
    private int codeLength;

    @Transactional(readOnly = true)
    public void sendEmailCode(EmailRequest dto) {
        String emailCode = CodeGenerator.generateHexCode(codeLength);
        String studentId = dto.getStudentId();
        Instant now = Instant.now(clock);
        log.info(now.toString());
        checkAlreadyStudentId(studentId);
        signupRedisRepository.setAuthPayload(studentId, EMAIL_AUTH_NAME, emailCode, now);

        String text = makeTemplatedEmail(
            dto.getStudentId(),
            emailCode
        );
        nhnEmailService.sendMessage(dto.getStudentId(), "단국대 학생 인증", text);
    }

    private void checkAlreadyStudentId(String studentId) {
        Optional<User> alreadyUser = userRepository.findByStudentId(studentId);
        if (alreadyUser.isPresent()) {
            throw new AlreadyStudentIdException();
        }
    }

    public StudentVerifyResponse validateEmailCode(EmailVerifyRequest request) {
        String signupToken = UUID.randomUUID().toString();

        Instant now = Instant.now(clock);
        String emailCode = signupRedisRepository.getAuthPayload(request.getStudentId(),
            EMAIL_AUTH_NAME,
            String.class,
            now).orElseThrow(RuntimeException::new);

        if (!emailCode.equalsIgnoreCase(request.getEmailCode())) {
            throw new EmailCodeNotMatchException();
        }

        UserInfo info = new UserInfo(request.getStudentId());

        signupRedisRepository.setAuthPayload(signupToken, DKU_AUTH_NAME, info, now);

        return new StudentVerifyResponse(info, signupToken);
    }

    public UserInfo getStudentInfo(String signupToken) {
        Instant now = Instant.now(clock);
        return signupRedisRepository.getAuthPayload(signupToken, DKU_AUTH_NAME, UserInfo.class, now)
            .orElseThrow(InvalidatedTokenException::new);
    }

    private String makeTemplatedEmail(String studentId, String buttonContent) {
        return new TextTemplateEngine.Builder()
            .argument("studentId", studentId)
            .argument("emailContent", "단국대학교 재학생 인증을 위해, 아래의 코드를\n입력해 주세요.")
            .argument("linkButtonContent", buttonContent)
            .build()
            .readHtmlFromResource("auth_email_content.html");
    }
}
