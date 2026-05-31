package com.stock.analysis.service;

import com.stock.analysis.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> permissionsHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> dataScopeHolder = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> allowedStocksHolder = new ThreadLocal<>();

    public static void set(Long userId, String username, Set<String> permissions, String dataScope, Set<String> allowedStocks) {
        userIdHolder.set(userId);
        usernameHolder.set(username);
        permissionsHolder.set(permissions);
        dataScopeHolder.set(dataScope);
        allowedStocksHolder.set(allowedStocks);
    }

    public static Long getUserId() { return userIdHolder.get(); }
    public static String getUsername() { return usernameHolder.get(); }
    public static Set<String> getPermissions() { return permissionsHolder.get(); }
    public static String getDataScope() { return dataScopeHolder.get(); }
    public static Set<String> getAllowedStocks() { return allowedStocksHolder.get(); }

    public static boolean hasPermission(String permission) {
        Set<String> perms = permissionsHolder.get();
        return perms != null && (perms.contains(permission) || perms.contains("*"));
    }

    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        permissionsHolder.remove();
        dataScopeHolder.remove();
        allowedStocksHolder.remove();
    }
}