package com.stock.analysis.controller;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.entity.Permission;
import com.stock.analysis.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {

    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    @RequirePermission("role:view")
    public ResponseEntity<Map<String, Object>> list() {
        List<Permission> perms = permissionRepository.findAllByOrderByModuleAsc();
        
        Map<String, List<Permission>> grouped = perms.stream()
                .collect(Collectors.groupingBy(p -> p.getModule() != null ? p.getModule() : "other"));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("id", "module_" + entry.getKey());
            group.put("name", getModuleLabel(entry.getKey()));
            group.put("children", entry.getValue().stream().map(p -> {
                Map<String, Object> child = new HashMap<>();
                child.put("id", p.getId());
                child.put("name", p.getName());
                child.put("code", p.getCode());
                child.put("description", p.getDescription());
                child.put("module", p.getModule());
                return child;
            }).collect(Collectors.toList()));
            tree.add(group);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", tree);
        result.put("flat", perms);
        result.put("total", perms.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/flat")
    public ResponseEntity<List<Permission>> flatList() {
        return ResponseEntity.ok(permissionRepository.findAllByOrderByModuleAsc());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> tree() {
        List<Permission> perms = permissionRepository.findAllByOrderByModuleAsc();
        Map<String, List<Permission>> grouped = perms.stream()
                .collect(Collectors.groupingBy(p -> p.getModule() != null ? p.getModule() : "other"));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("id", "module_" + entry.getKey());
            group.put("name", getModuleLabel(entry.getKey()));
            group.put("children", entry.getValue().stream().map(p -> {
                Map<String, Object> child = new HashMap<>();
                child.put("id", p.getId());
                child.put("name", p.getName() + " (" + p.getCode() + ")");
                return child;
            }).collect(Collectors.toList()));
            tree.add(group);
        }
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/{id}")
    @RequirePermission("role:view")
    public ResponseEntity<Permission> getById(@PathVariable Long id) {
        return permissionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission("role:assign")
    public ResponseEntity<Permission> create(@RequestBody Permission permission) {
        permission.setId(null);
        return ResponseEntity.ok(permissionRepository.save(permission));
    }

    @PutMapping("/{id}")
    @RequirePermission("role:assign")
    public ResponseEntity<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        Optional<Permission> optional = permissionRepository.findById(id);
        if (optional.isPresent()) {
            Permission existing = optional.get();
            existing.setName(permission.getName());
            existing.setDescription(permission.getDescription());
            existing.setModule(permission.getModule());
            return ResponseEntity.ok(permissionRepository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role:assign")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private String getModuleLabel(String module) {
        return switch (module) {
            case "stock" -> "股票管理";
            case "user" -> "用户管理";
            case "role" -> "角色管理";
            case "menu" -> "菜单管理";
            case "log" -> "日志管理";
            case "config" -> "配置管理";
            case "task" -> "任务管理";
            case "dataperm" -> "数据权限";
            default -> "其他";
        };
    }
}