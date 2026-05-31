package com.stock.analysis.config;

import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import com.stock.analysis.service.UserContext;
import com.stock.analysis.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataPermissionRepository dataPermRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        UserContext.clear();

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    loadUserContext(userId, username);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private void loadUserContext(Long userId, String username) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Set<String> permissions = new HashSet<>();
                for (Role role : user.getRoles()) {
                    for (Permission perm : role.getPermissions()) {
                        permissions.add(perm.getCode());
                    }
                }
                if ("ADMIN".equals(user.getRole()) || user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode()))) {
                    permissions.add("*");
                }

                Set<String> allowedStocks = new HashSet<>();
                String dataScope = user.getDataScope() != null ? user.getDataScope() : "ALL";
                if ("CUSTOM".equals(dataScope)) {
                    Optional<UserDataPermission> dpOpt = dataPermRepository.findByUserId(userId);
                    if (dpOpt.isPresent() && dpOpt.get().getAllowedStockCodes() != null) {
                        allowedStocks = Arrays.stream(dpOpt.get().getAllowedStockCodes().split(","))
                                .map(String::trim)
                                .collect(Collectors.toSet());
                    }
                }

                UserContext.set(userId, username, permissions, dataScope, allowedStocks);
            }
        } catch (Exception e) {
            // ignore context loading errors
        }
    }
}