package com.stock.analysis.controller;

import com.stock.analysis.annotation.RequirePermission;
import com.stock.analysis.entity.UserDataPermission;
import com.stock.analysis.entity.StockInfo;
import com.stock.analysis.repository.UserDataPermissionRepository;
import com.stock.analysis.repository.UserRepository;
import com.stock.analysis.repository.StockInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/data-permissions")
@CrossOrigin(origins = "*")
public class DataPermissionController {

    @Autowired
    private UserDataPermissionRepository dataPermRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockInfoRepository stockInfoRepository;

    @GetMapping
    @RequirePermission("dataperm:view")
    public ResponseEntity<Map<String, Object>> list() {
        List<UserDataPermission> list = dataPermRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    @RequirePermission("dataperm:view")
    public ResponseEntity<UserDataPermission> getByUserId(@PathVariable Long userId) {
        return dataPermRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(null));
    }

    @PostMapping
    @RequirePermission("dataperm:edit")
    public ResponseEntity<UserDataPermission> create(@RequestBody UserDataPermission dp) {
        dp.setId(null);
        return ResponseEntity.ok(dataPermRepository.save(dp));
    }

    @PutMapping("/{id}")
    @RequirePermission("dataperm:edit")
    public ResponseEntity<UserDataPermission> update(@PathVariable Long id, @RequestBody UserDataPermission dp) {
        Optional<UserDataPermission> optional = dataPermRepository.findById(id);
        if (optional.isPresent()) {
            UserDataPermission existing = optional.get();
            existing.setAllowedStockCodes(dp.getAllowedStockCodes());
            existing.setAllowedSectors(dp.getAllowedSectors());
            existing.setScope(dp.getScope());
            return ResponseEntity.ok(dataPermRepository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/user/{userId}")
    @RequirePermission("dataperm:edit")
    public ResponseEntity<UserDataPermission> saveByUserId(@PathVariable Long userId, @RequestBody UserDataPermission dp) {
        Optional<UserDataPermission> existing = dataPermRepository.findByUserId(userId);
        if (existing.isPresent()) {
            UserDataPermission e = existing.get();
            e.setAllowedStockCodes(dp.getAllowedStockCodes());
            e.setAllowedSectors(dp.getAllowedSectors());
            e.setScope(dp.getScope());
            return ResponseEntity.ok(dataPermRepository.save(e));
        } else {
            dp.setUserId(userId);
            dp.setId(null);
            return ResponseEntity.ok(dataPermRepository.save(dp));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission("dataperm:edit")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dataPermRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}")
    @RequirePermission("dataperm:edit")
    public ResponseEntity<Void> deleteByUserId(@PathVariable Long userId) {
        dataPermRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stocks")
    @RequirePermission("dataperm:view")
    public ResponseEntity<List<Map<String, String>>> stockOptions() {
        List<StockInfo> stocks = stockInfoRepository.findAll();
        List<Map<String, String>> options = new ArrayList<>();
        for (StockInfo s : stocks) {
            Map<String, String> opt = new HashMap<>();
            opt.put("code", s.getCode());
            opt.put("name", s.getName());
            options.add(opt);
        }
        return ResponseEntity.ok(options);
    }
}