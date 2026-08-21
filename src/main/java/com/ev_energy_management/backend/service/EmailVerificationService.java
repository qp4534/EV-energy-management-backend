package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.exception.EmailSendCooldownException;
import com.ev_energy_management.backend.exception.InvalidVerificationCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

// 회원가입 이메일 인증코드를 Redis에 저장/검증한다. TokenBlacklistService와 달리 이건
// 회원가입을 막는 보안 게이트라서, Redis 장애 시 조용히 넘어가지 않고 그대로 예외를 던진다
// (fail-closed) - "인증 없이도 그냥 가입되는" 상황을 만들지 않기 위함.
@Service
public class EmailVerificationService {

    private static final String CODE_PREFIX = "auth:email-code:";
    private static final String VERIFIED_PREFIX = "auth:email-verified:";
    private static final String COOLDOWN_PREFIX = "auth:email-cooldown:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    // 프론트(admin 앱 VerifyField.tsx, frontend-web SignupInfo.jsx/ResetPasswordRequest.jsx)의
    // 재전송 카운트다운 표시 시간과 반드시 맞춰야 한다 - 안 맞으면 카운트다운이 끝났는데도
    // 서버가 이 쿨다운에 걸려 재요청을 거부하는 상황이 생긴다.
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(120);

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    // ⚠️ SMTP 서버 대부분(Daum 포함)이 From 헤더 없는 메일을 거부한다(Gmail은 관대해서
    // 이 문제가 안 드러났었음). SimpleMailMessage는 setFrom()을 안 부르면 From을 아예
    // 안 채우므로 명시적으로 넣어야 한다. Daum/Gmail 때는 spring.mail.username 자체가
    // 로그인 이메일이라 그걸 그대로 From으로 썼었는데, SES SMTP username은 IAM access key
    // 파생값이라 이메일 형식이 아니어서 더 이상 재사용할 수 없다 - mail.from-address로 분리.
    @Value("${mail.from-address}")
    private String fromAddress;

    public EmailVerificationService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    public void sendCode(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_PREFIX + email))) {
            throw new EmailSendCooldownException("잠시 후 다시 시도해주세요.");
        }

        String code = generateCode();
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, CODE_TTL);
        redisTemplate.opsForValue().set(COOLDOWN_PREFIX + email, "1", RESEND_COOLDOWN);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("[MijungE] 이메일 인증번호");
        message.setText("인증번호는 " + code + " 입니다. 5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    public void verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new InvalidVerificationCodeException("인증번호가 올바르지 않거나 만료되었습니다.");
        }
        redisTemplate.delete(CODE_PREFIX + email);
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "1", VERIFIED_TTL);
    }

    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_PREFIX + email));
    }

    public void clearVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }

    private static String generateCode() {
        int code = new SecureRandom().nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
