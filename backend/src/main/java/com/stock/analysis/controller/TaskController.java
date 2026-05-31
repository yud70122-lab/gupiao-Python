package com.stock.analysis.controller;

import com.stock.analysis.entity.ScheduledTask;
import com.stock.analysis.repository.ScheduledTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private ScheduledTaskRepository taskRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        List<ScheduledTask> tasks = taskRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("list", tasks);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTask> getById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScheduledTask> create(@RequestBody ScheduledTask task) {
        task.setId(null);
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduledTask> update(@PathVariable Long id, @RequestBody ScheduledTask task) {
        Optional<ScheduledTask> optional = taskRepository.findById(id);
        if (optional.isPresent()) {
            ScheduledTask existing = optional.get();
            existing.setName(task.getName());
            existing.setTaskGroup(task.getTaskGroup());
            existing.setCron(task.getCron());
            existing.setTargetMethod(task.getTargetMethod());
            existing.setDescription(task.getDescription());
            return ResponseEntity.ok(taskRepository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<ScheduledTask> start(@PathVariable Long id) {
        Optional<ScheduledTask> optional = taskRepository.findById(id);
        if (optional.isPresent()) {
            ScheduledTask task = optional.get();
            task.setStatus("运行中");
            return ResponseEntity.ok(taskRepository.save(task));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/stop")
    public ResponseEntity<ScheduledTask> stop(@PathVariable Long id) {
        Optional<ScheduledTask> optional = taskRepository.findById(id);
        if (optional.isPresent()) {
            ScheduledTask task = optional.get();
            task.setStatus("已停止");
            return ResponseEntity.ok(taskRepository.save(task));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ScheduledTask> execute(@PathVariable Long id) {
        Optional<ScheduledTask> optional = taskRepository.findById(id);
        if (optional.isPresent()) {
            ScheduledTask task = optional.get();
            task.setLastExecuteTime(LocalDateTime.now());
            return ResponseEntity.ok(taskRepository.save(task));
        }
        return ResponseEntity.notFound().build();
    }
}
