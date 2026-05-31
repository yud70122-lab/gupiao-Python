package com.stock.analysis.controller;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    @RequirePermission("role:view")
    public ResponseEntity<Map<String, Object>> list() {
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> roleList = new ArrayList<>();
        for (Role r : roles) {
            roleList.add(toMap(r));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", roleList);
        result.put("total", roles.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> all() {
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Role r : roles) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("code", r.getCode());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @RequirePermission("role:view")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(r -> ResponseEntity.ok(toMap(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission("role:add")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Role role = new Role();
        role.setName((String) body.get("name"));
        role.setCode((String) body.get("code"));
        role.setDescription((String) body.get("description"));
        Role saved = roleRepository.save(role);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/{id}")
    @RequirePermission("role:edit")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Role> optional = roleRepository.findById(id);
        if (optional.isPresent()) {
            Role existing = optional.get();
            if (body.get("name") != null) existing.setName((String) body.get("name"));
            if (body.get("description") != null) existing.setDescription((String) body.get("description"));
            return ResponseEntity.ok(toMap(roleRepository.save(existing)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role:delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (roleRepository.findById(id).map(Role::getCode).orElse("").equals("ADMIN")) {
            return ResponseEntity.badRequest().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/permissions")
    @RequirePermission("role:assign")
    public ResponseEntity<Map<String, Object>> assignPermissions(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Role> optional = roleRepository.findById(id);
        if (optional.isPresent()) {
            Role role = optional.get();

            @SuppressWarnings("unchecked")
            List<Long> permissionIds = (List<Long>) body.get("permissionIds");
            if (permissionIds != null) {
                role.setPermissions(new HashSet<>(permissionRepository.findAllById(permissionIds)));
            }

            @SuppressWarnings("unchecked")
            List<Long> menuIds = (List<Long>) body.get("menuIds");
            if (menuIds != null) {
                role.setMenus(new HashSet<>(menuRepository.findAllById(menuIds)));
            }

            roleRepository.save(role);
            return ResponseEntity.ok(toMap(role));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/permissions")
    @RequirePermission("role:view")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long id) {
        Optional<Role> optional = roleRepository.findById(id);
        if (optional.isPresent()) {
            Role role = optional.get();
            Map<String, Object> result = new HashMap<>();
            result.put("permissionIds", role.getPermissions().stream().map(Permission::getId).toList());
            result.put("menuIds", role.getMenus().stream().map(Menu::getId).toList());
            result.put("permissions", role.getPermissions().stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("name", p.getName());
                m.put("code", p.getCode());
                return m;
            }).toList());
            result.put("menus", role.getMenus().stream().map(m -> {
                Map<String, Object> mm = new HashMap<>();
                mm.put("id", m.getId());
                mm.put("name", m.getName());
                mm.put("path", m.getPath());
                return mm;
            }).toList());
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    private Map<String, Object> toMap(Role r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("name", r.getName());
        map.put("code", r.getCode());
        map.put("description", r.getDescription());
        map.put("userCount", r.getUserCount());
        map.put("createTime", r.getCreateTime());
        map.put("menuIds", r.getMenus().stream().map(Menu::getId).toList());
        map.put("permissionIds", r.getPermissions().stream().map(Permission::getId).toList());
        return map;
    }
}