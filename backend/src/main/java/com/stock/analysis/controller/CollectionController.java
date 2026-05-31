package com.stock.analysis.controller;

import com.stock.analysis.dto.StockBasicInfo;
import com.stock.analysis.entity.CollectionBasicInfo;
import com.stock.analysis.entity.CollectionMarketData;
import com.stock.analysis.service.CollectionService;
import com.stock.analysis.service.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping("/basic")
    public ResponseEntity<?> getBasicInfo(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String industry) {
        List<CollectionBasicInfo> list = collectionService.getBasicInfoList(keyword, exchange, industry);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);
        result.put("industries", collectionService.getAllIndustries());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/basic/collect")
    public ResponseEntity<?> collectBasicInfo() {
        return ResponseEntity.ok(Map.of("message", "基础信息采集任务已启动", "success", true));
    }

    @GetMapping("/market/stocks")
    public ResponseEntity<?> getMarketStockList() {
        List<StockBasicInfo> stocks = collectionService.getMarketStockList();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", stocks);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/market")
    public ResponseEntity<?> getMarketData(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CollectionMarketData> data = collectionService.getMarketData(code, period, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/market/collect")
    public ResponseEntity<?> collectMarketData(@RequestParam String code) {
        return ResponseEntity.ok(Map.of("message", "行情数据采集任务已启动", "success", true, "code", code));
    }

    @GetMapping("/financial/stocks")
    public ResponseEntity<?> getFinancialStockList() {
        List<StockBasicInfo> stocks = collectionService.getFinancialStockList();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", stocks);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/financial")
    public ResponseEntity<?> getFinancialData(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "income") String reportType,
            @RequestParam(required = false) String period) {
        Map<String, Object> data = collectionService.getFinancialData(code, reportType, period);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.putAll(data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/financial/collect")
    public ResponseEntity<?> collectFinancialData(@RequestParam String code) {
        return ResponseEntity.ok(Map.of("message", "财务数据采集任务已启动", "success", true, "code", code));
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getMarketOverview() {
        Map<String, Object> data = collectionService.getMarketOverview();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.putAll(data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/overview/collect")
    public ResponseEntity<?> collectOverview() {
        return ResponseEntity.ok(Map.of("message", "市场数据采集任务已启动", "success", true));
    }
}
