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
import java.util.List;

@RestController
@RequestMapping("/api/quant")
@RequiredArgsConstructor
public class QuantAnalysisController {

    private final QuantCalculationService quantCalculationService;

    @PostMapping("/correlation")
    public ResponseEntity<CorrelationAnalysisResponse> calculateCorrelation(@RequestBody QuantRequest request) {
        CorrelationAnalysisResponse response = quantCalculationService.calculateCorrelation(request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<byte[]> exportCorrelationExcel(@RequestBody List<CorrelationDetailRow> rows) {
        byte[] excelData = quantCalculationService.exportCorrelationToExcel(rows);
        String fileName = URLEncoder.encode("相关性分析明细.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
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
