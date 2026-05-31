package com.stock.analysis.service;

import com.stock.analysis.dto.*;
import com.stock.analysis.entity.StockInfo;
import com.stock.analysis.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockDataService {

    private final StockInfoRepository stockInfoRepository;

    public List<StockBasicInfo> getAllStocks() {
        List<Object[]> results = stockInfoRepository.findAllDistinctStocks();
        return results.stream()
                .map(row -> new StockBasicInfo((String) row[0], (String) row[1]))
                .collect(Collectors.toList());
    }

    public List<StockBasicInfo> searchStocks(String keyword) {
        List<Object[]> results = stockInfoRepository.searchStocks(keyword);
        return results.stream()
                .map(row -> new StockBasicInfo((String) row[0], (String) row[1]))
                .collect(Collectors.toList());
    }

    public List<KLineData> getKLineData(String code, LocalDate startDate, LocalDate endDate) {
        List<StockInfo> stockInfos = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                code, startDate, endDate);
        return stockInfos.stream()
                .map(s -> new KLineData(
                        s.getTradeDate(),
                        s.getOpenPrice(),
                        s.getHighPrice(),
                        s.getLowPrice(),
                        s.getClosePrice(),
                        s.getVolume()
                ))
                .collect(Collectors.toList());
    }

    public List<ReturnAnalysis> getReturnAnalysis(String code, LocalDate startDate, LocalDate endDate) {
        List<StockInfo> stockInfos = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                code, startDate, endDate);

        String name = stockInfoRepository.findNameByCode(code).orElse(code);

        List<ReturnAnalysis> result = new ArrayList<>();
        double cumulativeReturn = 0.0;

        for (int i = 0; i < stockInfos.size(); i++) {
            StockInfo info = stockInfos.get(i);
            double dailyReturn = info.getDailyReturn() != null ? info.getDailyReturn() : 0.0;

            if (i == 0) {
                cumulativeReturn = dailyReturn;
            } else {
                cumulativeReturn = (1 + cumulativeReturn) * (1 + dailyReturn) - 1;
            }

            result.add(new ReturnAnalysis(
                    info.getTradeDate(),
                    code,
                    name,
                    dailyReturn,
                    cumulativeReturn
            ));
        }
        return result;
    }

    public VolatilityAnalysis getVolatilityAnalysis(String code, LocalDate startDate, LocalDate endDate) {
        List<StockInfo> stockInfos = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                code, startDate, endDate);

        String name = stockInfoRepository.findNameByCode(code).orElse(code);

        List<Double> returns = stockInfos.stream()
                .map(s -> s.getDailyReturn() != null ? s.getDailyReturn() : 0.0)
                .collect(Collectors.toList());

        double[] returnArray = returns.stream().mapToDouble(Double::doubleValue).toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(returnArray);

        double dailyVolatility = stats.getStandardDeviation();
        double annualizedVolatility = dailyVolatility * Math.sqrt(252);
        double averageReturn = stats.getMean();
        double sharpeRatio = (averageReturn * 252) / annualizedVolatility;

        double maxDrawdown = calculateMaxDrawdown(stockInfos);

        return new VolatilityAnalysis(
                code,
                name,
                dailyVolatility,
                annualizedVolatility,
                maxDrawdown,
                sharpeRatio,
                averageReturn
        );
    }

    private double calculateMaxDrawdown(List<StockInfo> stockInfos) {
        if (stockInfos.isEmpty()) return 0.0;

        double peak = stockInfos.get(0).getClosePrice();
        double maxDrawdown = 0.0;

        for (StockInfo info : stockInfos) {
            double close = info.getClosePrice();
            if (close > peak) {
                peak = close;
            }
            double drawdown = (peak - close) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        return maxDrawdown;
    }

    public List<CorrelationResult> getCorrelationMatrix(List<String> codes, LocalDate startDate, LocalDate endDate) {
        List<CorrelationResult> results = new ArrayList<>();
        Map<String, double[]> returnMap = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();

        for (String code : codes) {
            List<StockInfo> stockInfos = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                    code, startDate, endDate);
            double[] returns = stockInfos.stream()
                    .mapToDouble(s -> s.getDailyReturn() != null ? s.getDailyReturn() : 0.0)
                    .toArray();
            returnMap.put(code, returns);
            nameMap.put(code, stockInfoRepository.findNameByCode(code).orElse(code));
        }

        PearsonsCorrelation correlation = new PearsonsCorrelation();

        for (int i = 0; i < codes.size(); i++) {
            for (int j = i; j < codes.size(); j++) {
                String code1 = codes.get(i);
                String code2 = codes.get(j);
                double[] returns1 = returnMap.get(code1);
                double[] returns2 = returnMap.get(code2);

                int minLength = Math.min(returns1.length, returns2.length);
                double[] r1 = Arrays.copyOf(returns1, minLength);
                double[] r2 = Arrays.copyOf(returns2, minLength);

                double corr = 0.0;
                if (minLength > 2) {
                    corr = correlation.correlation(r1, r2);
                }

                results.add(new CorrelationResult(
                        code1, nameMap.get(code1),
                        code2, nameMap.get(code2),
                        corr
                ));
            }
        }
        return results;
    }

    public List<RiskReturnPoint> getRiskReturnScatter(List<String> codes, LocalDate startDate, LocalDate endDate) {
        List<RiskReturnPoint> points = new ArrayList<>();

        for (String code : codes) {
            VolatilityAnalysis volatility = getVolatilityAnalysis(code, startDate, endDate);
            points.add(new RiskReturnPoint(
                    code,
                    volatility.getName(),
                    volatility.getAnnualizedVolatility(),
                    volatility.getAverageReturn() * 252
            ));
        }
        return points;
    }

    public StockInfo saveStockInfo(StockInfo stockInfo) {
        return stockInfoRepository.save(stockInfo);
    }

    public List<StockInfo> saveAllStockInfos(List<StockInfo> stockInfos) {
        return stockInfoRepository.saveAll(stockInfos);
    }
}
