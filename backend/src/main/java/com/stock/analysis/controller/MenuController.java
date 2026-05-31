package com.stock.analysis.controller;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.entity.Menu;
import com.stock.analysis.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private MenuRepository menuRepository;

    @GetMapping
    @RequirePermission("menu:view")
    public ResponseEntity<Map<String, Object>> list() {
        List<Menu> allMenus = menuRepository.findAllByOrderBySortOrderAsc();
        List<Map<String, Object>> tree = buildTree(allMenus);
        Map<String, Object> result = new HashMap<>();
        result.put("list", tree);
        result.put("flat", allMenus);
        result.put("total", allMenus.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/flat")
    @RequirePermission("menu:view")
    public ResponseEntity<List<Menu>> flatList() {
        return ResponseEntity.ok(menuRepository.findAllByOrderBySortOrderAsc());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> tree() {
        List<Menu> allMenus = menuRepository.findByVisibleTrueOrderBySortOrderAsc();
        return ResponseEntity.ok(buildTree(allMenus));
    }

    @GetMapping("/{id}")
    @RequirePermission("menu:view")
    public ResponseEntity<Menu> getById(@PathVariable Long id) {
        return menuRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RequirePermission("menu:edit")
    public ResponseEntity<Menu> create(@RequestBody Menu menu) {
        menu.setId(null);
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @PutMapping("/{id}")
    @RequirePermission("menu:edit")
    public ResponseEntity<Menu> update(@PathVariable Long id, @RequestBody Menu menu) {
        Optional<Menu> optional = menuRepository.findById(id);
        if (optional.isPresent()) {
            Menu existing = optional.get();
            existing.setName(menu.getName());
            existing.setPath(menu.getPath());
            existing.setComponent(menu.getComponent());
            existing.setIcon(menu.getIcon());
            existing.setSortOrder(menu.getSortOrder());
            existing.setParentId(menu.getParentId());
            existing.setType(menu.getType());
            existing.setPermission(menu.getPermission());
            existing.setVisible(menu.getVisible());
            return ResponseEntity.ok(menuRepository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("menu:edit")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private List<Map<String, Object>> buildTree(List<Menu> menus) {
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Menu m : menus) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", m.getId());
            node.put("name", m.getName());
            node.put("path", m.getPath());
            node.put("component", m.getComponent());
            node.put("icon", m.getIcon());
            node.put("sortOrder", m.getSortOrder());
            node.put("parentId", m.getParentId());
            node.put("type", m.getType());
            node.put("permission", m.getPermission());
            node.put("visible", m.getVisible());
            node.put("children", new ArrayList<>());
            nodeMap.put(m.getId(), node);
        }

        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : nodeMap.values()) {
            Long parentId = (Long) node.get("parentId");
            if (parentId == null) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(parentId);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }
}