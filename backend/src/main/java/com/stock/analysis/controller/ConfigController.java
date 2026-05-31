package com.stock.analysis.controller;

import com.stock.analysis.entity.SystemConfig;
import com.stock.analysis.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/configs")
@CrossOrigin(origins = "*")
public class ConfigController {

    @Autowired
    private SystemConfigRepository configRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<SystemConfig> configs = configRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", configs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{key}")
    public ResponseEntity<SystemConfig> getByKey(@PathVariable String key) {
        return configRepository.findByConfigKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SystemConfig> save(@RequestBody SystemConfig config) {
        Optional<SystemConfig> existing = configRepository.findByConfigKey(config.getConfigKey());
        if (existing.isPresent()) {
            SystemConfig e = existing.get();
            e.setConfigValue(config.getConfigValue());
            e.setDescription(config.getDescription());
            return ResponseEntity.ok(configRepository.save(e));
        }
        return ResponseEntity.ok(configRepository.save(config));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        configRepository.deleteByConfigKey(key);
        return ResponseEntity.ok().build();
    }
}
