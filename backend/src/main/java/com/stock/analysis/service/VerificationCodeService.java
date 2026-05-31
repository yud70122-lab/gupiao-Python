package com.stock.analysis.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    private static final long EXPIRE_TIME = 5 * 60 * 1000;

    private static class CodeInfo {
        String code;
        long expireTime;

        CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

    public String generateCode(String key) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        codeStore.put(key, new CodeInfo(code, System.currentTimeMillis() + EXPIRE_TIME));
        return code;
    }

    public boolean verifyCode(String key, String code) {
        CodeInfo info = codeStore.get(key);
        if (info == null) return false;
        if (System.currentTimeMillis() > info.expireTime) {
            codeStore.remove(key);
            return false;
        }
        boolean result = info.code.equals(code);
        if (result) {
            codeStore.remove(key);
        }
        return result;
    }

    public void removeCode(String key) {
        codeStore.remove(key);
    }
}
