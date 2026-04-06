package com.project.apiGateWay;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
// public class JwtAuthFilter implements GlobalFilter, Ordered {
public class JwtAuthFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // 🔓 Allow auth endpoints
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // return exchange.getResponse().setComplete();
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // try {
        //     jwtUtil.isTokenValid(token);
        // } catch (Exception e) {
        //     exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        //     return exchange.getResponse().setComplete();
        // }

        String username = jwtUtil.extractEmail(token);
        List<? extends GrantedAuthority> authorities = jwtUtil.extractRole(token).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(exchange.getRequest()
                                .mutate()
                                .header("X-USER-ID", String.valueOf(jwtUtil.extractId(token)))
                                .header("X-USERNAME", username)
                                .build())
                        .build();

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}
