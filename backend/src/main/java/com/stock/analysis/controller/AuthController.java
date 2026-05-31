package com.stock.analysis.controller;

import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import com.stock.analysis.service.SessionManager;
import com.stock.analysis.service.VerificationCodeService;
import com.stock.analysis.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserDataPermissionRepository dataPermRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VerificationCodeService codeService;

    @Autowired
    private SessionManager sessionManager;

    @PostMapping("/send-code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String key = phone != null ? phone : email;

        if (key == null || key.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号或邮箱不能为空"));
        }

        String code = codeService.generateCode(key);

        System.out.println("验证码已发送到 " + key + ": " + code);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "验证码已发送");
        result.put("code", code);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String phone = request.get("phone");
        String email = request.get("email");
        String code = request.get("code");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名不能为空"));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码不能为空"));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名已存在"));
        }
        if (phone != null && userRepository.findByPhone(phone).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号已注册"));
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "邮箱已注册"));
        }

        String verifyKey = phone != null ? phone : email;
        if (verifyKey != null && code != null && !codeService.verifyCode(verifyKey, code)) {
            return ResponseEntity.badRequest().body(Map.of("message", "验证码错误或已过期"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRealName(username);
        user.setRole("访客");
        user.setEnabled(true);
        user.setDataScope("ALL");

        Optional<Role> viewerRole = roleRepository.findByCode("VIEWER");
        viewerRole.ifPresent(role -> user.getRoles().add(role));

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request,
                                                    HttpServletRequest httpRequest) {
        try {
        String username = request.get("username");
        String password = request.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名或密码错误"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名或密码错误"));
        }
        if (!user.getEnabled()) {
            return ResponseEntity.badRequest().body(Map.of("message", "账号已被禁用"));
        }

        String token = jwtUtil.generateToken(user.getId(), username);
        String ip = getClientIp(httpRequest);
        boolean isDifferentLocation = checkDifferentLocation(user, ip);

        sessionManager.registerSession(user.getId(), token, ip);

        user.setLastLoginIp(ip);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        Set<String> permissions = new HashSet<>();
        Set<String> menuPaths = new HashSet<>();
        for (Role role : user.getRoles()) {
            for (Permission perm : role.getPermissions()) {
                permissions.add(perm.getCode());
            }
            for (Menu menu : role.getMenus()) {
                if (menu.getPath() != null) {
                    menuPaths.add(menu.getPath());
                }
            }
        }
        if ("ADMIN".equals(user.getRole()) || user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode()))) {
            permissions.add("*");
        }

        Set<String> allowedStocks = new HashSet<>();
        String dataScope = user.getDataScope() != null ? user.getDataScope() : "ALL";
        if ("CUSTOM".equals(dataScope)) {
            Optional<UserDataPermission> dpOpt = dataPermRepository.findByUserId(user.getId());
            if (dpOpt.isPresent() && dpOpt.get().getAllowedStockCodes() != null) {
                allowedStocks = Arrays.stream(dpOpt.get().getAllowedStockCodes().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
            }
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        result.put("permissions", permissions);
        result.put("menus", menuPaths);
        result.put("dataScope", dataScope);
        result.put("allowedStocks", allowedStocks);
        result.put("differentLocation", isDifferentLocation);

        return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errResult = new HashMap<>();
            errResult.put("message", "登录失败: " + e.getClass().getName() + " - " + e.getMessage());
            return ResponseEntity.status(500).body(errResult);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = sessionManager.getUserIdByToken(token);
            if (userId != null) {
                sessionManager.invalidateSession(userId);
            }
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "退出成功"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        String code = request.get("code");

        String key = phone != null ? phone : email;
        if (key == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号或邮箱不能为空"));
        }

        if (!codeService.verifyCode(key, code)) {
            return ResponseEntity.badRequest().body(Map.of("message", "验证码错误或已过期"));
        }

        Optional<User> userOpt = phone != null ? userRepository.findByPhone(phone) : userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户不存在"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "密码重置成功"));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("id", user.getId());
                    profile.put("username", user.getUsername());
                    profile.put("realName", user.getRealName());
                    profile.put("email", user.getEmail());
                    profile.put("phone", user.getPhone());
                    profile.put("avatar", user.getAvatar());
                    profile.put("role", user.getRole());
                    profile.put("dataScope", user.getDataScope());
                    profile.put("roles", user.getRoles().stream().map(r -> Map.of(
                        "id", r.getId(),
                        "name", r.getName(),
                        "code", r.getCode()
                    )).toList());
                    return ResponseEntity.ok(profile);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                            @RequestBody Map<String, String> request) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (request.get("realName") != null) user.setRealName(request.get("realName"));
                    if (request.get("email") != null) user.setEmail(request.get("email"));
                    if (request.get("phone") != null) user.setPhone(request.get("phone"));
                    if (request.get("avatar") != null) user.setAvatar(request.get("avatar"));
                    userRepository.save(user);
                    user.setPassword(null);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("user", user);
                    return ResponseEntity.ok(result);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestHeader("Authorization") String authHeader,
                                                              @RequestBody Map<String, String> request) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    String oldPassword = request.get("oldPassword");
                    String newPassword = request.get("newPassword");

                    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        return ResponseEntity.badRequest().body(Map.of("message", "原密码错误"));
                    }

                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("success", true, "message", "密码修改成功"));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private boolean checkDifferentLocation(User user, String ip) {
        String lastIp = user.getLastLoginIp();
        if (lastIp == null || lastIp.equals(ip)) {
            return false;
        }
        String[] lastParts = lastIp.split("\\.");
        String[] currentParts = ip.split("\\.");
        if (lastParts.length >= 2 && currentParts.length >= 2) {
            return !(lastParts[0].equals(currentParts[0]) && lastParts[1].equals(currentParts[1]));
        }
        return true;
    }
}