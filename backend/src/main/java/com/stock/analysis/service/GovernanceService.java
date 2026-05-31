package com.stock.analysis.service;

import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GovernanceService {

    private final GovernanceBasicInfoRepository basicInfoRepository;
    private final GovernanceMarketDataRepository marketDataRepository;
    private final GovernanceFinancialDataRepository financialDataRepository;
    private final GovernanceMarketOverviewRepository marketOverviewRepository;

    public List<GovernanceBasicInfo> getBasicInfoList(String keyword, String status, String quality) {
        List<GovernanceBasicInfo> result = basicInfoRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            result = result.stream()
                    .filter(item -> item.getCode().contains(keyword) || item.getName().contains(keyword))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            result = result.stream()
                    .filter(item -> status.equals(item.getDataStatus()))
                    .collect(Collectors.toList());
        }
        if (quality != null && !quality.isEmpty()) {
            result = result.stream()
                    .filter(item -> quality.equals(item.getQualityLevel()))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public boolean auditBasicInfo(List<Long> ids, boolean approved, String comment) {
        for (Long id : ids) {
            Optional<GovernanceBasicInfo> opt = basicInfoRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceBasicInfo info = opt.get();
                info.setDataStatus(approved ? "approved" : "rejected");
                basicInfoRepository.save(info);
            }
        }
        return true;
    }

    public boolean cleanBasicInfo(List<Long> ids) {
        for (Long id : ids) {
            Optional<GovernanceBasicInfo> opt = basicInfoRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceBasicInfo info = opt.get();
                info.setCompleteness(Math.min(100, info.getCompleteness() + 10));
                info.setAccuracy(Math.min(100, info.getAccuracy() + 10));
                info.setDataStatus("approved");
                info.setQualityLevel(upgradeQuality(info.getQualityLevel()));
                basicInfoRepository.save(info);
            }
        }
        return true;
    }

    private String upgradeQuality(String current) {
        return switch (current) {
            case "D" -> "C";
            case "C" -> "B";
            case "B" -> "A";
            default -> "A";
        };
    }

    public Map<String, Object> getMarketDataOverview() {
        Map<String, Object> result = new HashMap<>();

        List<GovernanceMarketData> allData = marketDataRepository.findAll();
        long total = allData.size();
        long normalCount = allData.stream().filter(d -> "normal".equals(d.getDataStatus())).count();
        long abnormalCount = allData.stream().filter(d -> "abnormal".equals(d.getDataStatus())).count();
        long pendingCount = allData.stream().filter(d -> "pending".equals(d.getDataStatus())).count();

        result.put("completeness", total > 0 ? Math.round((double) (total - abnormalCount) / total * 1000.0) / 10.0 : 0);
        result.put("accuracy", total > 0 ? Math.round((double) normalCount / total * 1000.0) / 10.0 : 0);
        result.put("abnormalCount", (int) abnormalCount);
        result.put("pendingCount", (int) pendingCount);

        return result;
    }

    public List<GovernanceMarketData> getMarketDataList(String code, String status, String quality) {
        List<GovernanceMarketData> result;

        if (code != null && !code.isEmpty()) {
            if (status != null && !status.isEmpty() && quality != null && !quality.isEmpty()) {
                result = marketDataRepository.findByCodeAndDataStatusAndQualityLevelOrderByTradeDateDesc(code, status, quality);
            } else if (status != null && !status.isEmpty()) {
                result = marketDataRepository.findByCodeAndDataStatusOrderByTradeDateDesc(code, status);
            } else if (quality != null && !quality.isEmpty()) {
                result = marketDataRepository.findByCodeAndQualityLevelOrderByTradeDateDesc(code, quality);
            } else {
                result = marketDataRepository.findByCodeOrderByTradeDateDesc(code);
            }
        } else {
            result = marketDataRepository.findAll();
        }

        return result;
    }

    public boolean auditMarketData(List<Long> ids, boolean approved) {
        for (Long id : ids) {
            Optional<GovernanceMarketData> opt = marketDataRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceMarketData data = opt.get();
                data.setDataStatus(approved ? "normal" : "abnormal");
                data.setQualityLevel(approved ? "A" : "D");
                marketDataRepository.save(data);
            }
        }
        return true;
    }

    public boolean fixMarketData(List<Long> ids) {
        for (Long id : ids) {
            Optional<GovernanceMarketData> opt = marketDataRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceMarketData data = opt.get();
                data.setDataStatus("normal");
                data.setQualityLevel("B");
                data.setAbnormalType(null);
                marketDataRepository.save(data);
            }
        }
        return true;
    }

    public List<GovernanceFinancialData> getFinancialDataList(String code, String reportType, String status) {
        List<GovernanceFinancialData> result;

        if (status != null && !status.isEmpty()) {
            result = financialDataRepository.findByCodeAndReportTypeAndDataStatusOrderByPeriodDesc(code, reportType, status);
        } else {
            result = financialDataRepository.findByCodeAndReportTypeOrderByPeriodDesc(code, reportType);
        }

        return result;
    }

    public boolean auditFinancialData(List<Long> ids, boolean approved) {
        for (Long id : ids) {
            Optional<GovernanceFinancialData> opt = financialDataRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceFinancialData data = opt.get();
                data.setDataStatus(approved ? "synced" : "abnormal");
                data.setVerifyStatus(approved ? "pass" : "fail");
                financialDataRepository.save(data);
            }
        }
        return true;
    }

    public boolean standardizeFinancialData(List<Long> ids) {
        for (Long id : ids) {
            Optional<GovernanceFinancialData> opt = financialDataRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceFinancialData data = opt.get();
                data.setDataStatus("synced");
                financialDataRepository.save(data);
            }
        }
        return true;
    }

    public boolean verifyFinancialData(List<Long> ids) {
        for (Long id : ids) {
            Optional<GovernanceFinancialData> opt = financialDataRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceFinancialData data = opt.get();
                data.setVerifyStatus("pass");
                financialDataRepository.save(data);
            }
        }
        return true;
    }

    public Map<String, Object> getMarketOverviewIndex(String indexType, String status) {
        Map<String, Object> result = new HashMap<>();

        List<GovernanceMarketOverview> indexData = marketOverviewRepository.findByDataTypeOrderByTradeDateDesc("INDEX");

        if (indexType != null && !indexType.isEmpty()) {
            Map<String, String> typeMap = new HashMap<>();
            typeMap.put("sh", "000001");
            typeMap.put("sz", "399001");
            typeMap.put("cyb", "399006");
            String indexCode = typeMap.get(indexType);
            if (indexCode != null) {
                if (status != null && !status.isEmpty()) {
                    indexData = marketOverviewRepository.findByDataTypeAndIndexCodeAndDataStatusOrderByTradeDateDesc("INDEX", indexCode, status);
                } else {
                    indexData = marketOverviewRepository.findByDataTypeAndIndexCodeOrderByTradeDateDesc("INDEX", indexCode);
                }
            }
        }

        List<Map<String, Object>> indexList = indexData.stream()
                .map(idx -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", idx.getId());
                    map.put("date", idx.getTradeDate().toString());
                    map.put("indexCode", idx.getIndexCode());
                    map.put("indexName", idx.getIndexName());
                    map.put("open", idx.getOpenPrice());
                    map.put("high", idx.getHighPrice());
                    map.put("low", idx.getLowPrice());
                    map.put("close", idx.getClosePrice());
                    map.put("change", idx.getChangePercent());
                    map.put("volume", idx.getVolume());
                    map.put("dataStatus", idx.getDataStatus());
                    return map;
                })
                .collect(Collectors.toList());

        result.put("indexData", indexList);
        return result;
    }

    public List<Map<String, Object>> getMarketOverviewNorthbound(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<GovernanceMarketOverview> northData = marketOverviewRepository.findByDataTypeAndTradeDateBetweenOrderByTradeDateAsc(
                "NORTHBOUND", startDate, endDate);

        return northData.stream()
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", n.getTradeDate().toString());
                    map.put("shNetBuy", n.getShNetBuy());
                    map.put("szNetBuy", n.getSzNetBuy());
                    map.put("totalNetBuy", n.getTotalNetBuy());
                    map.put("shBuy", n.getShBuy());
                    map.put("shSell", n.getShSell());
                    map.put("szBuy", n.getSzBuy());
                    map.put("szSell", n.getSzSell());
                    map.put("dataStatus", n.getDataStatus());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMarketOverviewStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("upCount", 2356);
        result.put("downCount", 2108);
        result.put("flatCount", 325);
        result.put("limitUp", 68);
        result.put("limitDown", 23);
        return result;
    }

    public boolean auditMarketOverview(List<Long> ids, boolean approved) {
        for (Long id : ids) {
            Optional<GovernanceMarketOverview> opt = marketOverviewRepository.findById(id);
            if (opt.isPresent()) {
                GovernanceMarketOverview data = opt.get();
                data.setDataStatus(approved ? "normal" : "abnormal");
                marketOverviewRepository.save(data);
            }
        }
        return true;
    }

    public boolean mergeMarketOverview(List<Long> ids) {
        if (ids.size() > 1) {
            for (int i = 1; i < ids.size(); i++) {
                marketOverviewRepository.deleteById(ids.get(i));
            }
        }
        return true;
    }
}
