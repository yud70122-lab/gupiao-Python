package com.stock.analysis.controller;

import com.stock.analysis.entity.CollectionLog;
import com.stock.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatisticsController {

    private final CollectionBasicInfoRepository basicInfoRepository;
    private final CollectionMarketDataRepository marketDataRepository;
    private final CollectionFinancialDataRepository financialDataRepository;
    private final CollectionMarketOverviewRepository marketOverviewRepository;
    private final CollectionLogRepository collectionLogRepository;
    private final StockInfoRepository stockInfoRepository;

    @GetMapping("/data-status")
    public ResponseEntity<Map<String, Object>> getDataStatus() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dataTypes = new ArrayList<>();

        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("type", "basic");
        basicInfo.put("name", "基础信息");
        basicInfo.put("totalCount", basicInfoRepository.count());
        basicInfo.put("coverage", calculateCoverage(basicInfoRepository.count(), 5000));
        dataTypes.add(basicInfo);

        Map<String, Object> marketData = new HashMap<>();
        marketData.put("type", "market");
        marketData.put("name", "行情数据");
        marketData.put("totalCount", marketDataRepository.count());
        marketData.put("coverage", calculateCoverage(marketDataRepository.count(), 50000));
        dataTypes.add(marketData);

        Map<String, Object> financialData = new HashMap<>();
        financialData.put("type", "financial");
        financialData.put("name", "财务数据");
        financialData.put("totalCount", financialDataRepository.count());
        financialData.put("coverage", calculateCoverage(financialDataRepository.count(), 10000));
        dataTypes.add(financialData);

        Map<String, Object> marketOverview = new HashMap<>();
        marketOverview.put("type", "overview");
        marketOverview.put("name", "市场概览");
        marketOverview.put("totalCount", marketOverviewRepository.count());
        marketOverview.put("coverage", calculateCoverage(marketOverviewRepository.count(), 100));
        dataTypes.add(marketOverview);

        Map<String, Object> stockInfo = new HashMap<>();
        stockInfo.put("type", "stock");
        stockInfo.put("name", "股票分析数据");
        stockInfo.put("totalCount", stockInfoRepository.count());
        stockInfo.put("coverage", calculateCoverage(stockInfoRepository.count(), 100000));
        dataTypes.add(stockInfo);

        result.put("dataTypes", dataTypes);
        result.put("totalRecords", basicInfoRepository.count() + marketDataRepository.count() + 
                                financialDataRepository.count() + marketOverviewRepository.count() + stockInfoRepository.count());
        result.put("lastUpdateTime", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getCollectionLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataType) {

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<CollectionLog> page;

        if (status != null && !status.isEmpty() && dataType != null && !dataType.isEmpty()) {
            page = collectionLogRepository.findByStatusAndDataType(status, dataType, pageable);
        } else if (status != null && !status.isEmpty()) {
            page = collectionLogRepository.findByStatus(status, pageable);
        } else if (dataType != null && !dataType.isEmpty()) {
            page = collectionLogRepository.findByDataType(dataType, pageable);
        } else {
            page = collectionLogRepository.findAll(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("failCount", collectionLogRepository.countByStatus("失败"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logs/{id}/retry")
    public ResponseEntity<Map<String, Object>> retryCollection(@PathVariable Long id) {
        Optional<CollectionLog> logOpt = collectionLogRepository.findById(id);
        if (logOpt.isPresent()) {
            CollectionLog log = logOpt.get();
            log.setStatus("重试中");
            log.setRetryCount(log.getRetryCount() + 1);
            collectionLogRepository.save(log);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "重试任务已启动");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/logs/retry-batch")
    public ResponseEntity<Map<String, Object>> retryBatch(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            Optional<CollectionLog> logOpt = collectionLogRepository.findById(id);
            if (logOpt.isPresent()) {
                CollectionLog log = logOpt.get();
                log.setStatus("重试中");
                log.setRetryCount(log.getRetryCount() + 1);
                collectionLogRepository.save(log);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量重试任务已启动");
        result.put("count", ids.size());
        return ResponseEntity.ok(result);
    }

    private double calculateCoverage(long actual, long expected) {
        if (expected == 0) return 0;
        double coverage = (double) actual / expected * 100;
        return Math.min(coverage, 100);
    }
}
