package com.ev_energy_management.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SignupRequest(
        @NotBlank(message = "이메일을 입력해주세요.") String email,
        @NotBlank(message = "비밀번호를 입력해주세요.") String password,
        @NotBlank(message = "이름을 입력해주세요.") String name,
        @NotBlank(message = "전화번호를 입력해주세요.") String phone,
        // 프론트(SignupInfo.jsx)에서 년/월/일 드롭다운 중 하나라도 안 고르면 빈 문자열("")을
        // 그대로 보낸다 - 검증 없이 그냥 저장 시도하면 UserEntity.birth의 not-null 제약에서
        // 500으로 죽는다(DB 예외가 그대로 사용자에게 노출됨). 여기서 먼저 걸러서 400으로 응답한다.
        @NotNull(message = "생년월일을 선택해주세요.") LocalDate birth,
        @NotBlank(message = "가입 유형을 선택해주세요.") String role,
        List<String> consentedTerms
) {}
