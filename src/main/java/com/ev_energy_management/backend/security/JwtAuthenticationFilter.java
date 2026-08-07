package com.ev_energy_management.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // USER.role의 실제 값(한글)을 Spring Security 권한으로 매핑한다. 목록에 없는 값(예: "이용자")은
    // 관리자/관제자 전용 규칙의 대상이 아니라는 뜻으로 일반 ROLE_USER만 부여해 authenticated()는
    // 통과하되 hasRole("ADMIN")/hasRole("CONTROLLER") 같은 특정 role 체크는 통과 못 하게 한다.
    private static final Map<String, String> ROLE_TO_AUTHORITY = Map.of(
            "관리자", "ROLE_ADMIN",
            "관제자", "ROLE_CONTROLLER"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            if (!tokenBlacklistService.isBlacklisted(token)) {
                Optional<AuthenticatedUser> authenticatedUser = jwtTokenProvider.parseToken(token);
                authenticatedUser.ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(toAuthority(user.role())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }
        // Missing/invalid/blacklisted(logged-out) tokens simply leave the request
        // unauthenticated instead of rejecting it here, so today's permitAll endpoints
        // keep working unauthenticated.
        filterChain.doFilter(request, response);
    }

    private static GrantedAuthority toAuthority(String role) {
        return new SimpleGrantedAuthority(ROLE_TO_AUTHORITY.getOrDefault(role, "ROLE_USER"));
    }
}
