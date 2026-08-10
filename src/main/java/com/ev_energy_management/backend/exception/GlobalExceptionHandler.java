package com.ev_energy_management.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "VALIDATION_ERROR", "입력값을 확인해주세요.", fieldErrors));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "INVALID_PASSWORD", e.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "EMAIL_ALREADY_EXISTS", e.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "INVALID_CREDENTIALS", e.getMessage()));
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "ACCOUNT_LOCKED", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "UNAUTHORIZED", "인증이 필요합니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "FORBIDDEN", "권한이 없습니다."));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "INVALID_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceUnavailable(AiServiceUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(503, "AI_SERVICE_UNAVAILABLE", e.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "INVALID_VERIFICATION_CODE", e.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "EMAIL_NOT_VERIFIED", e.getMessage()));
    }

    @ExceptionHandler(EmailSendCooldownException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendCooldown(EmailSendCooldownException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(429, "EMAIL_SEND_COOLDOWN", e.getMessage()));
    }

    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<ErrorResponse> handleAccountDeleted(AccountDeletedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "ACCOUNT_DELETED", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // 회원가입 등에서 유니크 제약(이메일/전화번호 중복) 위반 시 원본 DB 예외 메시지가 그대로
        // 500으로 노출되던 걸 막는다 - 사용자에게는 무엇이 중복인지 정도만 알려준다.
        String msg = e.getMostSpecificCause().getMessage();
        String field = msg != null && msg.contains("phone") ? "전화번호"
                : msg != null && msg.contains("email") ? "이메일" : "입력값";
        log.warn("Data integrity violation: {}", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "DUPLICATE_VALUE", "이미 사용 중인 " + field + "입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
        // was previously swallowed silently - nothing showed up anywhere when this fired
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}
