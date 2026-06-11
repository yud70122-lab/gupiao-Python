package com.stock.analysis.controller;

import com.stock.analysis.dto.*;
import com.stock.analysis.entity.SectorInfo;
import com.stock.analysis.service.QuantCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/quant")
@RequiredArgsConstructor
public class QuantAnalysisController {

    private final QuantCalculationService quantCalculationService;

    private final ConcurrentHashMap<Long, CorrelationScheme> schemeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> schemeTimeStore = new ConcurrentHashMap<>();
    private long schemeIdCounter = 0;

    @PostMapping("/correlation/sync")
    public ResponseEntity<CorrelationAnalysisResponse> calculateCorrelationSync(@RequestBody QuantRequest request) {
        CorrelationAnalysisResponse response = quantCalculationService.calculateCorrelation(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/correlation")
    public ResponseEntity<CorrelationAnalysisResponse> calculateCorrelation(@RequestBody QuantRequest request) {
        CorrelationAnalysisResponse response = quantCalculationService.calculateCorrelation(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/correlation/async/submit")
    public ResponseEntity<AsyncTaskSubmitResponse> submitAsyncTask(@RequestBody QuantRequest request) {
        AsyncTaskSubmitResponse response = quantCalculationService.submitAsyncTask(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/correlation/async/query")
    public ResponseEntity<AsyncTaskQueryResponse> queryAsyncTask(@RequestParam String taskId) {
        AsyncTaskQueryResponse response = quantCalculationService.queryAsyncTask(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/correlation/async/stop")
    public ResponseEntity<Map<String, Object>> stopAsyncTask(@RequestBody Map<String, String> body) {
        String taskId = body.get("taskId");
        boolean stopped = quantCalculationService.stopAsyncTask(taskId);
        return ResponseEntity.ok(Map.of("success", stopped, "message", stopped ? "任务已停止" : "任务不存在"));
    }

    @PostMapping("/correlation/scheme")
    public ResponseEntity<Map<String, Object>> saveScheme(@RequestBody CorrelationScheme scheme) {
        scheme.setId(++schemeIdCounter);
        scheme.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        schemeStore.put(scheme.getId(), scheme);
        return ResponseEntity.ok(Map.of("success", true, "id", scheme.getId(), "message", "方案保存成功"));
    }

    @GetMapping("/correlation/scheme")
    public ResponseEntity<List<CorrelationScheme>> listSchemes() {
        return ResponseEntity.ok(new ArrayList<>(schemeStore.values()));
    }

    @DeleteMapping("/correlation/scheme/{id}")
    public ResponseEntity<Map<String, Object>> deleteScheme(@PathVariable Long id) {
        CorrelationScheme removed = schemeStore.remove(id);
        boolean success = removed != null;
        return ResponseEntity.ok(Map.of("success", success, "message", success ? "方案已删除" : "方案不存在"));
    }

    @PostMapping("/export/correlation/excel")
    public ResponseEntity<byte[]> exportCorrelationExcel(@RequestBody List<CorrelationDetailRow> rows) {
        byte[] excelData = quantCalculationService.exportCorrelationToExcel(rows);
        String fileName = URLEncoder.encode("相关性分析明细.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping("/export/correlation/csv")
    public ResponseEntity<byte[]> exportCorrelationCSV(@RequestBody List<CorrelationDetailRow> rows) {
        byte[] csvData = quantCalculationService.exportCorrelationToCSV(rows);
        String fileName = URLEncoder.encode("相关性分析明细.csv", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @PostMapping("/linkage")
    public ResponseEntity<LinkageAnalysisResponse> calculateLinkage(@RequestBody QuantRequest request) {
        LinkageAnalysisResponse response = quantCalculationService.calculateLinkage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<SectorInfo>> getAllSectors() {
        return ResponseEntity.ok(quantCalculationService.getAllSectors());
    }

    @GetMapping("/indices")
    public ResponseEntity<List<Object[]>> getAllIndices() {
        return ResponseEntity.ok(quantCalculationService.getAllIndices());
    }

    @PostMapping("/export/correlation")
    public ResponseEntity<byte[]> exportCorrelationExcelLegacy(@RequestBody List<CorrelationDetailRow> rows) {
        return exportCorrelationExcel(rows);
    }

    @PostMapping("/export/linkage")
    public ResponseEntity<byte[]> exportLinkageExcel(@RequestBody List<SectorLinkageRank> rows) {
        byte[] excelData = quantCalculationService.exportLinkageToExcel(rows);
        String fileName = URLEncoder.encode("板块联动排名.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }
}
