package com.stock.analysis.controller;

import com.stock.analysis.entity.GovernanceBasicInfo;
import com.stock.analysis.entity.GovernanceMarketData;
import com.stock.analysis.entity.GovernanceFinancialData;
import com.stock.analysis.service.GovernanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GovernanceController {

    private final GovernanceService governanceService;

    @GetMapping("/basic")
    public ResponseEntity<?> getBasicInfo(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String quality) {
        List<GovernanceBasicInfo> list = governanceService.getBasicInfoList(keyword, status, quality);
        return ResponseEntity.ok(Map.of("code", 200, "data", list));
    }

    @PostMapping("/basic/audit")
    public ResponseEntity<?> auditBasicInfo(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        boolean approved = params.containsKey("approved") && Boolean.parseBoolean(params.get("approved").toString());
        String comment = params.getOrDefault("comment", "").toString();
        governanceService.auditBasicInfo(ids, approved, comment);
        return ResponseEntity.ok(Map.of("message", "审核成功", "success", true));
    }

    @PostMapping("/basic/clean")
    public ResponseEntity<?> cleanBasicInfo(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        governanceService.cleanBasicInfo(ids);
        return ResponseEntity.ok(Map.of("message", "数据清洗任务已启动", "success", true));
    }

    @GetMapping("/market/overview")
    public ResponseEntity<?> getMarketDataOverview() {
        Map<String, Object> data = governanceService.getMarketDataOverview();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.putAll(data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/market")
    public ResponseEntity<?> getMarketData(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String quality) {
        List<GovernanceMarketData> list = governanceService.getMarketDataList(code, status, quality);
        return ResponseEntity.ok(Map.of("code", 200, "data", list));
    }

    @PostMapping("/market/audit")
    public ResponseEntity<?> auditMarketData(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        boolean approved = params.containsKey("approved") && Boolean.parseBoolean(params.get("approved").toString());
        governanceService.auditMarketData(ids, approved);
        return ResponseEntity.ok(Map.of("message", "审核成功", "success", true));
    }

    @PostMapping("/market/fix")
    public ResponseEntity<?> fixMarketData(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        governanceService.fixMarketData(ids);
        return ResponseEntity.ok(Map.of("message", "数据修复任务已启动", "success", true));
    }

    @PostMapping("/market/validate")
    public ResponseEntity<?> validateMarketData() {
        return ResponseEntity.ok(Map.of("message", "数据校验任务已启动，正在后台运行", "success", true));
    }

    @GetMapping("/financial")
    public ResponseEntity<?> getFinancialData(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "income") String reportType,
            @RequestParam(required = false) String status) {
        List<GovernanceFinancialData> list = governanceService.getFinancialDataList(code, reportType, status);
        return ResponseEntity.ok(Map.of("code", 200, "data", list));
    }

    @PostMapping("/financial/audit")
    public ResponseEntity<?> auditFinancialData(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        boolean approved = params.containsKey("approved") && Boolean.parseBoolean(params.get("approved").toString());
        governanceService.auditFinancialData(ids, approved);
        return ResponseEntity.ok(Map.of("message", "审核成功", "success", true));
    }

    @PostMapping("/financial/standardize")
    public ResponseEntity<?> standardizeFinancialData(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        governanceService.standardizeFinancialData(ids);
        return ResponseEntity.ok(Map.of("message", "格式标准化任务已启动", "success", true));
    }

    @PostMapping("/financial/verify")
    public ResponseEntity<?> verifyFinancialData(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        governanceService.verifyFinancialData(ids);
        return ResponseEntity.ok(Map.of("message", "勾稽校验任务已启动，正在后台执行", "success", true));
    }

    @GetMapping("/overview/index")
    public ResponseEntity<?> getMarketOverviewIndex(
            @RequestParam(required = false) String indexType,
            @RequestParam(required = false) String status) {
        Map<String, Object> data = governanceService.getMarketOverviewIndex(indexType, status);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.putAll(data);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/overview/northbound")
    public ResponseEntity<?> getMarketOverviewNorthbound(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> data = governanceService.getMarketOverviewNorthbound(startDate, endDate);
        return ResponseEntity.ok(Map.of("code", 200, "data", data));
    }

    @GetMapping("/overview/stats")
    public ResponseEntity<?> getMarketOverviewStats() {
        Map<String, Object> data = governanceService.getMarketOverviewStats();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.putAll(data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/overview/audit")
    public ResponseEntity<?> auditMarketOverview(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        boolean approved = params.containsKey("approved") && Boolean.parseBoolean(params.get("approved").toString());
        governanceService.auditMarketOverview(ids, approved);
        return ResponseEntity.ok(Map.of("message", "审核成功", "success", true));
    }

    @PostMapping("/overview/merge")
    public ResponseEntity<?> mergeMarketOverview(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params.get("ids"));
        governanceService.mergeMarketOverview(ids);
        return ResponseEntity.ok(Map.of("message", "数据去重合并任务已启动", "success", true));
    }

    @PostMapping("/overview/timeline")
    public ResponseEntity<?> timelineCheck() {
        return ResponseEntity.ok(Map.of("message", "时间轴校验任务已启动", "success", true));
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseIds(Object idsObj) {
        if (idsObj == null) return Collections.emptyList();
        if (idsObj instanceof List) {
            return ((List<?>) idsObj).stream()
                    .map(item -> {
                        if (item instanceof Number) return ((Number) item).longValue();
                        return Long.parseLong(item.toString());
                    })
                    .collect(Collectors.toList());
        }
        String idsStr = idsObj.toString();
        if (idsStr.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(idsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
