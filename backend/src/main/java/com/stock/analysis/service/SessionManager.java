package com.stock.analysis.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

    private final ConcurrentHashMap<Long, String> userTokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tokenUsers = new ConcurrentHashMap<>();

    public void registerSession(Long userId, String token, String ip) {
        String oldToken = userTokens.put(userId, token);
        if (oldToken != null) {
            tokenUsers.remove(oldToken);
        }
        tokenUsers.put(token, userId);
    }

    public boolean isTokenValid(Long userId, String token) {
        String activeToken = userTokens.get(userId);
        return token.equals(activeToken);
    }

    public void invalidateSession(Long userId) {
        String token = userTokens.remove(userId);
        if (token != null) {
            tokenUsers.remove(token);
        }
    }

    public Long getUserIdByToken(String token) {
        return tokenUsers.get(token);
    }
}
