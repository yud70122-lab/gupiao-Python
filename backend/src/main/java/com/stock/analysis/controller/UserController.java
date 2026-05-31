package com.stock.analysis.controller;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.entity.Role;
import com.stock.analysis.entity.User;
import com.stock.analysis.repository.RoleRepository;
import com.stock.analysis.repository.UserRepository;
import com.stock.analysis.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @RequirePermission("user:view")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<User> page;

        if (keyword != null && !keyword.isEmpty()) {
            page = userRepository.findByUsernameContainingOrRealNameContaining(keyword, keyword, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        List<Map<String, Object>> userList = new ArrayList<>();
        for (User u : page.getContent()) {
            userList.add(toMap(u));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", userList);
        result.put("total", page.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toMap(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission("user:add")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        User user = new User();
        user.setUsername((String) body.get("username"));
        user.setPassword(passwordEncoder.encode((String) body.getOrDefault("password", "123456")));
        user.setRealName((String) body.get("realName"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        user.setRole((String) body.getOrDefault("role", "访客"));
        user.setEnabled(Boolean.TRUE.equals(body.get("enabled")));
        user.setDataScope((String) body.getOrDefault("dataScope", "ALL"));

        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) body.get("roleIds");
        if (roleIds != null && !roleIds.isEmpty()) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("user:edit")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User existing = optional.get();
            if (body.get("realName") != null) existing.setRealName((String) body.get("realName"));
            if (body.get("email") != null) existing.setEmail((String) body.get("email"));
            if (body.get("phone") != null) existing.setPhone((String) body.get("phone"));
            if (body.get("role") != null) existing.setRole((String) body.get("role"));
            if (body.get("enabled") != null) existing.setEnabled(Boolean.TRUE.equals(body.get("enabled")));
            if (body.get("dataScope") != null) existing.setDataScope((String) body.get("dataScope"));

            @SuppressWarnings("unchecked")
            List<Long> roleIds = (List<Long>) body.get("roleIds");
            if (roleIds != null) {
                existing.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));
            }

            if (body.get("password") != null && !((String) body.get("password")).isEmpty()) {
                existing.setPassword(passwordEncoder.encode((String) body.get("password")));
            }

            return ResponseEntity.ok(toMap(userRepository.save(existing)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if ("admin".equals(userRepository.findById(id).map(User::getUsername).orElse(null))) {
            return ResponseEntity.badRequest().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle")
    @RequirePermission("user:toggle")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User user = optional.get();
            user.setEnabled(!user.getEnabled());
            return ResponseEntity.ok(toMap(userRepository.save(user)));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/current/permissions")
    public ResponseEntity<Map<String, Object>> currentPermissions() {
        Map<String, Object> result = new HashMap<>();
        result.put("permissions", UserContext.getPermissions());
        result.put("dataScope", UserContext.getDataScope());
        result.put("allowedStocks", UserContext.getAllowedStocks());
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(User u) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("username", u.getUsername());
        map.put("realName", u.getRealName());
        map.put("email", u.getEmail());
        map.put("phone", u.getPhone());
        map.put("role", u.getRole());
        map.put("enabled", u.getEnabled());
        map.put("dataScope", u.getDataScope());
        map.put("lastLoginIp", u.getLastLoginIp());
        map.put("lastLoginTime", u.getLastLoginTime());
        map.put("createTime", u.getCreateTime());
        map.put("roleIds", u.getRoles().stream().map(Role::getId).toList());
        map.put("roleNames", u.getRoles().stream().map(Role::getName).toList());
        return map;
    }
}