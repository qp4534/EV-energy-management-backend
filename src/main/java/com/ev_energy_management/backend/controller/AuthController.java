package com.ev_energy_management.backend.controller;

import com.ev_energy_management.backend.dto.auth.DeleteAccountRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailRequest;
import com.ev_energy_management.backend.dto.auth.FindEmailResponse;
import com.ev_energy_management.backend.dto.auth.LoginRequest;
import com.ev_energy_management.backend.dto.auth.LoginResponse;
import com.ev_energy_management.backend.dto.auth.MeResponse;
import com.ev_energy_management.backend.dto.ImageUploadUrlRequest;
import com.ev_energy_management.backend.dto.ImageUploadUrlResponse;
import com.ev_energy_management.backend.dto.auth.PasswordResetRequest;
import com.ev_energy_management.backend.dto.auth.ProfileUpdateRequest;
import com.ev_energy_management.backend.dto.auth.SendEmailCodeRequest;
import com.ev_energy_management.backend.dto.auth.SignupRequest;
import com.ev_energy_management.backend.dto.auth.VerifyEmailCodeRequest;
import com.ev_energy_management.backend.security.AuthenticatedUser;
import com.ev_energy_management.backend.service.AuthService;
import com.ev_energy_management.backend.service.EmailVerificationService;
import com.ev_energy_management.backend.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final S3Service s3Service;

    public AuthController(
            AuthService authService,
            EmailVerificationService emailVerificationService,
            S3Service s3Service
    ) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.s3Service = s3Service;
    }

    @PostMapping("/email/send-code")
    public ResponseEntity<Void> sendEmailCode(@RequestBody SendEmailCodeRequest request) {
        authService.ensureEmailAvailable(request.email());
        emailVerificationService.sendCode(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<Void> verifyEmailCode(@RequestBody VerifyEmailCodeRequest request) {
        emailVerificationService.verifyCode(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/find-email")
    public FindEmailResponse findEmail(@RequestBody FindEmailRequest request) {
        return authService.findEmail(request);
    }

    @PostMapping("/password/reset/send-code")
    public ResponseEntity<Void> sendPasswordResetCode(@RequestBody SendEmailCodeRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<MeResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return authService.login(request, ipAddress, userAgent);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        authService.logout(user, authorizationHeader.substring(BEARER_PREFIX.length()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.getMe(user);
    }

    @PatchMapping("/me")
    public MeResponse updateMe(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(user, request);
    }

    @PostMapping("/me/profile-image/upload-url")
    public ImageUploadUrlResponse createProfileImageUploadUrl(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ImageUploadUrlRequest request
    ) {
        return s3Service.createProfileImageUploadUrl(user.userId(), request.contentType());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody DeleteAccountRequest request
    ) {
        authService.deleteAccount(user, request, authorizationHeader.substring(BEARER_PREFIX.length()));
        return ResponseEntity.noContent().build();
    }
}
