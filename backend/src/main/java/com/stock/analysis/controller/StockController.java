package com.stock.analysis.controller;

import com.stock.analysis.dto.*;
import com.stock.analysis.service.StockDataService;
import com.stock.analysis.service.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockDataService stockDataService;

    @GetMapping
    public ResponseEntity<?> getAllStocks() {
        List<StockBasicInfo> stocks = stockDataService.getAllStocks();
        Set<String> allowed = UserContext.getAllowedStocks();
        String scope = UserContext.getDataScope();

        if (scope != null && "CUSTOM".equals(scope) && allowed != null && !allowed.isEmpty()) {
            stocks = stocks.stream()
                    .filter(s -> allowed.contains(s.getCode()))
                    .toList();
        }

        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchStocks(@RequestParam String keyword) {
        List<StockBasicInfo> stocks = stockDataService.searchStocks(keyword);
        Set<String> allowed = UserContext.getAllowedStocks();
        String scope = UserContext.getDataScope();

        if (scope != null && "CUSTOM".equals(scope) && allowed != null && !allowed.isEmpty()) {
            stocks = stocks.stream()
                    .filter(s -> allowed.contains(s.getCode()))
                    .toList();
        }

        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{code}/kline")
    public ResponseEntity<?> getKLineData(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!checkStockPermission(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "无权访问该股票数据"));
        }
        return ResponseEntity.ok(stockDataService.getKLineData(code, startDate, endDate));
    }

    @GetMapping("/{code}/returns")
    public ResponseEntity<?> getReturnAnalysis(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!checkStockPermission(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "无权访问该股票数据"));
        }
        return ResponseEntity.ok(stockDataService.getReturnAnalysis(code, startDate, endDate));
    }

    @GetMapping("/{code}/volatility")
    public ResponseEntity<?> getVolatilityAnalysis(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!checkStockPermission(code)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "无权访问该股票数据"));
        }
        return ResponseEntity.ok(stockDataService.getVolatilityAnalysis(code, startDate, endDate));
    }

    @PostMapping("/correlation")
    public ResponseEntity<?> getCorrelationMatrix(
            @RequestParam List<String> codes,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<String> allowedCodes = filterCodesByPermission(codes);
        if (allowedCodes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "无权访问所请求的股票数据"));
        }
        return ResponseEntity.ok(stockDataService.getCorrelationMatrix(allowedCodes, startDate, endDate));
    }

    @PostMapping("/risk-return")
    public ResponseEntity<?> getRiskReturnScatter(
            @RequestParam List<String> codes,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<String> allowedCodes = filterCodesByPermission(codes);
        if (allowedCodes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "无权访问所请求的股票数据"));
        }
        return ResponseEntity.ok(stockDataService.getRiskReturnScatter(allowedCodes, startDate, endDate));
    }

    private boolean checkStockPermission(String code) {
        String scope = UserContext.getDataScope();
        if (scope == null || !"CUSTOM".equals(scope)) {
            return true;
        }
        Set<String> allowed = UserContext.getAllowedStocks();
        return allowed != null && (allowed.isEmpty() || allowed.contains(code));
    }

    private List<String> filterCodesByPermission(List<String> codes) {
        String scope = UserContext.getDataScope();
        if (scope == null || !"CUSTOM".equals(scope)) {
            return codes;
        }
        Set<String> allowed = UserContext.getAllowedStocks();
        if (allowed == null || allowed.isEmpty()) {
            return codes;
        }
        return codes.stream().filter(allowed::contains).toList();
    }
}