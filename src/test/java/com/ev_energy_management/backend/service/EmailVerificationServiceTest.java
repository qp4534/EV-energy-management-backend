package com.ev_energy_management.backend.service;

import com.ev_energy_management.backend.exception.EmailSendCooldownException;
import com.ev_energy_management.backend.exception.InvalidVerificationCodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationService service;

    @Test
    void sendCodeStoresCodeAndCooldownThenSendsMail() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.hasKey("auth:email-cooldown:new@user.com")).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.sendCode("new@user.com");

        verify(valueOperations).set(eq("auth:email-code:new@user.com"), anyString(), eq(Duration.ofMinutes(5)));
        verify(valueOperations).set(eq("auth:email-cooldown:new@user.com"), eq("1"), eq(Duration.ofSeconds(60)));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendCodeRejectedDuringCooldown() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.hasKey("auth:email-cooldown:new@user.com")).thenReturn(true);

        assertThrows(EmailSendCooldownException.class, () -> service.sendCode("new@user.com"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void verifyCodeMarksEmailVerifiedOnMatch() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:email-code:new@user.com")).thenReturn("123456");

        service.verifyCode("new@user.com", "123456");

        verify(redisTemplate).delete("auth:email-code:new@user.com");
        verify(valueOperations).set("auth:email-verified:new@user.com", "1", Duration.ofMinutes(30));
    }

    @Test
    void verifyCodeRejectsWrongCode() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:email-code:new@user.com")).thenReturn("123456");

        assertThrows(InvalidVerificationCodeException.class, () -> service.verifyCode("new@user.com", "000000"));
    }

    @Test
    void verifyCodeRejectsExpiredOrMissingCode() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:email-code:new@user.com")).thenReturn(null);

        assertThrows(InvalidVerificationCodeException.class, () -> service.verifyCode("new@user.com", "123456"));
    }

    @Test
    void isVerifiedReflectsRedisFlag() {
        service = new EmailVerificationService(redisTemplate, mailSender);
        when(redisTemplate.hasKey("auth:email-verified:new@user.com")).thenReturn(true);

        assertTrue(service.isVerified("new@user.com"));
    }
}
