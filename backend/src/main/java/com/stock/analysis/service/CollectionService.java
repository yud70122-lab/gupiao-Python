package com.stock.analysis.service;

import com.stock.analysis.dto.StockBasicInfo;
import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionBasicInfoRepository basicInfoRepository;
    private final CollectionMarketDataRepository marketDataRepository;
    private final CollectionFinancialDataRepository financialDataRepository;
    private final CollectionMarketOverviewRepository marketOverviewRepository;

    public List<CollectionBasicInfo> getBasicInfoList(String keyword, String exchange, String industry) {
        List<CollectionBasicInfo> result = basicInfoRepository.findAll();

        if (keyword != null && !keyword.isEmpty()) {
            result = result.stream()
                    .filter(item -> item.getCode().contains(keyword) || item.getName().contains(keyword))
                    .collect(Collectors.toList());
        }
        if (exchange != null && !exchange.isEmpty()) {
            result = result.stream()
                    .filter(item -> exchange.equals(item.getExchange()))
                    .collect(Collectors.toList());
        }
        if (industry != null && !industry.isEmpty()) {
            result = result.stream()
                    .filter(item -> industry.equals(item.getIndustry()))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public List<String> getAllIndustries() {
        return basicInfoRepository.findAllDistinctIndustries();
    }

    public List<StockBasicInfo> getMarketStockList() {
        return marketDataRepository.findAllDistinctStocks();
    }

    public List<CollectionMarketData> getMarketData(String code, String period, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(120);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        return marketDataRepository.findByCodeAndPeriodAndTradeDateBetweenOrderByTradeDateAsc(code, period, startDate, endDate);
    }

    public List<StockBasicInfo> getFinancialStockList() {
        return financialDataRepository.findAllDistinctStocks();
    }

    public Map<String, Object> getFinancialData(String code, String reportType, String period) {
        Map<String, Object> result = new HashMap<>();

        List<CollectionFinancialData> dataList;
        if (period != null && !period.isEmpty()) {
            dataList = financialDataRepository.findByCodeAndReportTypeAndReportPeriodContainingOrderByReportPeriodDesc(code, reportType, period);
        } else {
            dataList = financialDataRepository.findByCodeAndReportTypeOrderByReportPeriodDesc(code, reportType);
        }

        if (!dataList.isEmpty()) {
            CollectionFinancialData latest = dataList.get(0);
            List<Map<String, Object>> keyMetrics = new ArrayList<>();

            keyMetrics.add(createMetric("pe", "市盈率(PE)", latest.getPe() != null ? latest.getPe().toString() : "-",
                    latest.getPe() != null && latest.getPe() > 30 ? "up" : "down",
                    latest.getPe() != null ? latest.getPe() * 0.1 : 0));
            keyMetrics.add(createMetric("pb", "市净率(PB)", latest.getPb() != null ? latest.getPb().toString() : "-",
                    latest.getPb() != null && latest.getPb() > 3.5 ? "up" : "down",
                    latest.getPb() != null ? latest.getPb() * 0.1 : 0));
            keyMetrics.add(createMetric("roe", "净资产收益率(ROE)", latest.getRoe() != null ? latest.getRoe() + "%" : "-",
                    latest.getRoe() != null && latest.getRoe() > 10 ? "up" : "down",
                    latest.getRoe() != null ? latest.getRoe() * 0.1 : 0));
            keyMetrics.add(createMetric("eps", "每股收益(EPS)", latest.getEps() != null ? latest.getEps() + "元" : "-",
                    latest.getEps() != null && latest.getEps() > 3 ? "up" : "down",
                    latest.getEps() != null ? latest.getEps() * 0.1 : 0));
            keyMetrics.add(createMetric("revenue", "营业收入", latest.getTotalRevenue() != null ? (latest.getTotalRevenue() / 10000.0) + "亿" : "-",
                    "up", 5.0));
            keyMetrics.add(createMetric("netProfit", "净利润", latest.getNetProfit() != null ? (latest.getNetProfit() / 10000.0) + "亿" : "-",
                    "up", 8.0));
            keyMetrics.add(createMetric("grossMargin", "毛利率", latest.getGrossMargin() != null ? latest.getGrossMargin() + "%" : "-",
                    "up", 3.0));
            keyMetrics.add(createMetric("debtRatio", "资产负债率", latest.getDebtRatio() != null ? latest.getDebtRatio() + "%" : "-",
                    "down", 2.0));

            result.put("keyMetrics", keyMetrics);
            result.put("incomeData", filterByReportType(dataList, "income"));
            result.put("balanceData", filterByReportType(dataList, "balance"));
            result.put("cashflowData", filterByReportType(dataList, "cashflow"));

            List<Map<String, Object>> valuationTrend = new ArrayList<>();
            String[] quarters = {"2025Q1", "2025Q2", "2025Q3", "2025Q4", "2026Q1"};
            for (int i = 0; i < quarters.length; i++) {
                Map<String, Object> point = new HashMap<>();
                point.put("quarter", quarters[i]);
                point.put("pe", latest.getPe() != null ? Math.round(latest.getPe() * (1 - i * 0.03) * 100.0) / 100.0 : 0);
                point.put("pb", latest.getPb() != null ? Math.round(latest.getPb() * (1 - i * 0.02) * 100.0) / 100.0 : 0);
                valuationTrend.add(point);
            }
            result.put("valuationTrend", valuationTrend);
        }

        return result;
    }

    private Map<String, Object> createMetric(String key, String label, String value, String trend, double change) {
        Map<String, Object> metric = new HashMap<>();
        metric.put("key", key);
        metric.put("label", label);
        metric.put("value", value);
        metric.put("trend", trend);
        metric.put("change", Math.round(change * 100.0) / 100.0);
        return metric;
    }

    private List<Map<String, Object>> filterByReportType(List<CollectionFinancialData> dataList, String type) {
        return dataList.stream()
                .filter(d -> type.equals(d.getReportType()))
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reportDate", d.getReportPeriod());
                    map.put("updateTime", d.getUpdateTime());
                    if ("income".equals(type)) {
                        map.put("totalRevenue", d.getTotalRevenue());
                        map.put("operatingCost", d.getOperatingCost());
                        map.put("operatingProfit", d.getOperatingProfit());
                        map.put("totalProfit", d.getTotalProfit());
                        map.put("netProfit", d.getNetProfit());
                        map.put("netProfitParent", d.getNetProfitParent());
                        map.put("eps", d.getEps());
                    } else if ("balance".equals(type)) {
                        map.put("totalAssets", d.getTotalAssets());
                        map.put("totalLiabilities", d.getTotalLiabilities());
                        map.put("totalEquity", d.getTotalEquity());
                        map.put("currentAssets", d.getCurrentAssets());
                        map.put("currentLiabilities", d.getCurrentLiabilities());
                        map.put("debtRatio", d.getDebtRatio());
                    } else if ("cashflow".equals(type)) {
                        map.put("operatingCashFlow", d.getOperatingCashFlow());
                        map.put("investingCashFlow", d.getInvestingCashFlow());
                        map.put("financingCashFlow", d.getFinancingCashFlow());
                        map.put("netCashFlow", d.getNetCashFlow());
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMarketOverview() {
        Map<String, Object> result = new HashMap<>();

        CollectionMarketOverview latestStats = marketOverviewRepository.findLatestByDataType("MARKET_STATS");
        if (latestStats != null) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("upCount", latestStats.getUpCount());
            stats.put("downCount", latestStats.getDownCount());
            stats.put("flatCount", latestStats.getFlatCount());
            stats.put("upPercent", latestStats.getUpPercent());
            stats.put("downPercent", latestStats.getDownPercent());
            stats.put("flatPercent", latestStats.getFlatPercent());
            stats.put("northFlow", latestStats.getNorthFlow());
            result.put("marketStats", stats);
        }

        List<CollectionMarketOverview> indexList = marketOverviewRepository.findByDataType("INDEX");
        List<Map<String, Object>> indexResult = indexList.stream()
                .collect(Collectors.groupingBy(CollectionMarketOverview::getIndexCode))
                .entrySet().stream()
                .map(entry -> {
                    List<CollectionMarketOverview> values = entry.getValue();
                    CollectionMarketOverview latest = values.get(0);
                    Map<String, Object> idx = new HashMap<>();
                    idx.put("code", latest.getIndexCode());
                    idx.put("name", latest.getIndexName());
                    idx.put("price", latest.getClosePrice());
                    idx.put("change", latest.getChangePercent());
                    idx.put("changePercent", latest.getChangePercent());
                    idx.put("volume", latest.getVolume());
                    idx.put("amount", latest.getAmount());
                    return idx;
                })
                .collect(Collectors.toList());
        result.put("indexList", indexResult);

        List<CollectionMarketOverview> heatList = marketOverviewRepository.findByDataType("HEAT");
        List<Map<String, Object>> heatResult = heatList.stream()
                .sorted((a, b) -> Double.compare(b.getStockTurnover() != null ? b.getStockTurnover() : 0,
                        a.getStockTurnover() != null ? a.getStockTurnover() : 0))
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", h.getStockCode());
                    map.put("name", h.getStockName());
                    map.put("price", h.getStockPrice());
                    map.put("change", h.getStockChange());
                    map.put("turnover", h.getStockTurnover());
                    map.put("volumeRatio", h.getVolumeRatio());
                    map.put("amount", h.getStockAmount());
                    map.put("updateTime", h.getUpdateTime());
                    return map;
                })
                .collect(Collectors.toList());
        result.put("heatData", heatResult);

        List<CollectionMarketOverview> northList = marketOverviewRepository.findByDataTypeAndTradeDateBetweenOrderByTradeDateAsc(
                "NORTHBOUND", LocalDate.now().minusDays(30), LocalDate.now());
        List<Map<String, Object>> northResult = northList.stream()
                .map(n -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", n.getTradeDate().toString());
                    map.put("netFlow", n.getTotalNetBuy());
                    map.put("buy", n.getShBuy() != null ? n.getShBuy() + n.getSzBuy() : 0);
                    map.put("sell", n.getShSell() != null ? n.getShSell() + n.getSzSell() : 0);
                    map.put("shNetBuy", n.getShNetBuy());
                    map.put("szNetBuy", n.getSzNetBuy());
                    map.put("shBuy", n.getShBuy());
                    map.put("shSell", n.getShSell());
                    map.put("szBuy", n.getSzBuy());
                    map.put("szSell", n.getSzSell());
                    map.put("totalNetBuy", n.getTotalNetBuy());
                    return map;
                })
                .collect(Collectors.toList());
        result.put("northData", northResult);

        return result;
    }
}
