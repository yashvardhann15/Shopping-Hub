package com.project.userservicejwt.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.userservicejwt.Service.JWTService;
import com.project.userservicejwt.Service.MyUserDetailsService;
import com.project.userservicejwt.Service.RedisService;
import com.project.userservicejwt.models.Token;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtService.extractUserName(token);
        }

        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(email);

//            var isValidToken = tokenRepository.findByToken(token)
//                    .map(t -> !t.isExpired() && !t.isRevoked())
//                    .orElse(false);
            boolean isValidToken = false;
            List<?> rawList = redisService.get("TOKEN_" + email, List.class);
            List<Token> tokens = new ArrayList<>();

            if (rawList != null) {
                ObjectMapper mapper = new ObjectMapper();
                tokens = rawList.stream()
                        .map(item -> mapper.convertValue(item, Token.class))
                        .collect(Collectors.toList());
            }

            for(Token tokenItem : tokens) {
                if(tokenItem.getToken().equals(token)) {
                    if(!tokenItem.isExpired() && !tokenItem.isRevoked()) {
                        isValidToken = true;
                    }
                }
            }

            if(jwtService.validateToken(token, userDetails) && isValidToken) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails , null , userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
