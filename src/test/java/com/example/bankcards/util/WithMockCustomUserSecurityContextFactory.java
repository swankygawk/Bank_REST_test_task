package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User principal = User.builder()
            .id(UUID.fromString(customUser.id()))
            .username(customUser.username())
            .build();

        List<SimpleGrantedAuthority> authorities = Stream.of(customUser.roles())
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            principal,
            "password",
            authorities
        );

        context.setAuthentication(token);
        return context;
    }
}
