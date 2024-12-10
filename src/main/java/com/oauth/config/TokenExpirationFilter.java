package com.oauth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class TokenExpirationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(TokenExpirationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof OAuth2AccessToken) {
            OAuth2AccessToken accessToken = (OAuth2AccessToken) authentication.getCredentials();

            if (accessToken.getExpiresAt() != null && accessToken.getExpiresAt().isBefore(Instant.now())) {
                logger.warn("Token has expired for user: {}", authentication.getName());
                response.sendRedirect("/error/token-expired");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}