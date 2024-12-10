package com.oauth.config;

import com.oauth.service.SocialAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebSecurityConfig {

    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
    private final SocialAppService socialAppService;

    public WebSecurityConfig(SocialAppService socialAppService) {
        this.socialAppService = socialAppService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new TokenExpirationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/profile").hasAuthority("USER")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/error/**").permitAll()
                        .anyRequest().permitAll()
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // Обработка ошибки 403: перенаправление на страницу /error/403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access Denied for '{}'. Remote IP: {}",
                                    request.getRequestURI(), request.getRemoteAddr());
                            response.sendRedirect("/error/403");
                        })
                        // Обработка ошибки 401: перенаправление на страницу /error/401
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Unauthorized access attempt to '{}'. Remote IP: {}",
                                    request.getRequestURI(), request.getRemoteAddr());
                            response.sendRedirect("/error/401");
                        })
                )

                .oauth2Login(oauth2 -> oauth2
                            .loginPage("/")
                            .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                    .userService(socialAppService))
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userAuthoritiesMapper(this::mapAuthorities))
                        .successHandler((request, response, authentication) -> {
                            if (authentication != null && authentication.getName() != null) {
                                logger.info("User '{}' successfully logged in.", authentication.getName());
                            }
                            response.sendRedirect("/profile");
                        })

                )

                .logout(logout -> {
                    logout
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .logoutUrl("/logout")
                            .logoutSuccessHandler((request, response, authentication) -> {
                                if (authentication != null && authentication.getName() != null) {
                                    logger.info("User '{}' successfully logged out.", authentication.getName());
                                    response.sendRedirect("/");
                                }
                            });
                });
        return http.build();
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            String rawAuthority = authority.getAuthority();

            if (rawAuthority.startsWith("OAUTH2_")) {
                String mappedRole = rawAuthority.substring(7);
                mappedAuthorities.add(new SimpleGrantedAuthority(mappedRole));
            } else {
                mappedAuthorities.add(authority);
            }
        }
        return mappedAuthorities;
    }
}