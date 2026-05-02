package com.seaman.filter;

import com.seaman.service.JwtTokenService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class TokenFilterConfiguerer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final JwtTokenService jwtTokenService;

    private final MessageCodeService messageCodeService;

    @Override
    public void configure(HttpSecurity http) {
        TokenFilter filter = new TokenFilter(jwtTokenService, messageCodeService);
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }
}
