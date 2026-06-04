package com.stock.analysis.service;

import com.stock.analysis.dto.*;
import com.stock.analysis.entity.IndexData;
import com.stock.analysis.entity.QuantCalculationResult;
import com.stock.analysis.entity.SectorInfo;
import com.stock.analysis.entity.StockInfo;
import com.stock.analysis.repository.IndexDataRepository;
import com.stock.analysis.repository.QuantCalculationResultRepository;
import com.stock.analysis.repository.SectorInfoRepository;
import com.stock.analysis.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuantCalculationService {

    private final StockInfoRepository stockInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final SectorInfoRepository sectorInfoRepository;
    private final QuantCalculationResultRepository quantResultRepository;

    private final PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
    private final SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();

    public CorrelationAnalysisResponse calculateCorrelation(QuantRequest request) {
        log.info("开始计算相关性分析，股票代码: {}, 数据类型: {}, 计算模式: {}",
                request.getStockCodes(), request.getDataType(), request.getCalculationMode());

        List<String> stockCodes = request.getStockCodes();
        if (stockCodes == null || stockCodes.size() < 2) {
            throw new IllegalArgumentException("至少需要选择2只股票进行相关性分析");
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        String dataType = request.getDataType();
        String calculationMode = request.getCalculationMode();
        Integer rollingWindow = request.getRollingWindow();

        Map<String, List<StockInfo>> stockDataMap = new HashMap<>();
        Map<String, String> stockNameMap = new HashMap<>();

        for (String code : stockCodes) {
            List<StockInfo> data = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                    code, startDate, endDate);
            data = cleanStockData(data);
            stockDataMap.put(code, data);
            stockNameMap.put(code, stockInfoRepository.findNameByCode(code).orElse(code));
            log.debug("股票 {} 清洗后数据量: {}", code, data.size());
        }

        List<CorrelationOverview> overviews = new ArrayList<>();
        List<CorrelationMatrixCell> correlationMatrix = new ArrayList<>();
        List<RollingCorrelationPoint> rollingCorrelation = new ArrayList<>();
        List<CorrelationDetailRow> detailRows = new ArrayList<>();

        for (int i = 0; i < stockCodes.size(); i++) {
            for (int j = i; j < stockCodes.size(); j++) {
                String codeA = stockCodes.get(i);
                String codeB = stockCodes.get(j);

                List<StockInfo> dataA = stockDataMap.get(codeA);
                List<StockInfo> dataB = stockDataMap.get(codeB);

                double[] seriesA = extractDataSeries(dataA, dataType);
                double[] seriesB = extractDataSeries(dataB, dataType);

                int minLength = Math.min(seriesA.length, seriesB.length);
                double[] alignedA = Arrays.copyOfRange(seriesA, 0, minLength);
                double[] alignedB = Arrays.copyOfRange(seriesB, 0, minLength);

                double pearson = 0.0;
                double spearman = 0.0;

                if (minLength > 2) {
                    pearson = pearsonsCorrelation.correlation(alignedA, alignedB);
                    spearman = spearmansCorrelation.correlation(alignedA, alignedB);
                }

                if (i != j) {
                    CorrelationOverview overview = new CorrelationOverview();
                    overview.setStockCodeA(codeA);
                    overview.setStockNameA(stockNameMap.get(codeA));
                    overview.setStockCodeB(codeB);
                    overview.setStockNameB(stockNameMap.get(codeB));
                    overview.setStatStartDate(startDate);
                    overview.setStatEndDate(endDate);
                    overview.setDataType(dataType);
                    overview.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
                    overview.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
                    overview.setCorrelationDescription(getCorrelationDescription(pearson));
                    overviews.add(overview);

                    saveQuantResult("CORRELATION", codeA, stockNameMap.get(codeA),
                            codeB, stockNameMap.get(codeB), startDate, endDate,
                            dataType, "BOTH", pearson, spearman, null, null);
                }

                CorrelationMatrixCell matrixCell = new CorrelationMatrixCell();
                matrixCell.setStockCodeX(codeA);
                matrixCell.setStockNameX(stockNameMap.get(codeA));
                matrixCell.setStockCodeY(codeB);
                matrixCell.setStockNameY(stockNameMap.get(codeB));
                matrixCell.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
                matrixCell.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
                correlationMatrix.add(matrixCell);

                if (i != j) {
                    CorrelationMatrixCell symmetricCell = new CorrelationMatrixCell();
                    symmetricCell.setStockCodeX(codeB);
                    symmetricCell.setStockNameX(stockNameMap.get(codeB));
                    symmetricCell.setStockCodeY(codeA);
                    symmetricCell.setStockNameY(stockNameMap.get(codeA));
                    symmetricCell.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
                    symmetricCell.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
                    correlationMatrix.add(symmetricCell);
                }
            }
        }

        if ("ROLLING".equals(calculationMode) && rollingWindow != null && rollingWindow > 0) {
            String codeA = stockCodes.get(0);
            String codeB = stockCodes.get(1);
            List<StockInfo> dataA = stockDataMap.get(codeA);
            List<StockInfo> dataB = stockDataMap.get(codeB);

            rollingCorrelation = calculateRollingCorrelation(
                    dataA, dataB, dataType, rollingWindow, codeA, codeB,
                    stockNameMap.get(codeA), stockNameMap.get(codeB), detailRows);
        } else if (stockCodes.size() >= 2) {
            String codeA = stockCodes.get(0);
            String codeB = stockCodes.get(1);
            List<StockInfo> dataA = stockDataMap.get(codeA);
            List<StockInfo> dataB = stockDataMap.get(codeB);
            generateDetailRows(dataA, dataB, dataType, codeA, codeB,
                    stockNameMap.get(codeA), stockNameMap.get(codeB), detailRows);
        }

        CorrelationAnalysisResponse response = new CorrelationAnalysisResponse();
        response.setOverviews(overviews);
        response.setCorrelationMatrix(correlationMatrix);
        response.setMatrixStockCodes(stockCodes);
        response.setMatrixStockNames(stockCodes.stream().map(stockNameMap::get).collect(Collectors.toList()));
        response.setRollingCorrelation(rollingCorrelation);
        response.setDetailRows(detailRows);

        log.info("相关性分析计算完成，概览数: {}, 矩阵单元格数: {}, 明细行数: {}",
                overviews.size(), correlationMatrix.size(), detailRows.size());

        return response;
    }

    public LinkageAnalysisResponse calculateLinkage(QuantRequest request) {
        log.info("开始计算联动性分析");

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        String dataType = request.getDataType();
        Integer rollingWindow = request.getRollingWindow();

        List<SectorInfo> sectors = sectorInfoRepository.findAll();
        if (request.getSectorCode() != null && !request.getSectorCode().isEmpty()) {
            sectors = sectors.stream()
                    .filter(s -> s.getSectorCode().equals(request.getSectorCode()))
                    .collect(Collectors.toList());
        }

        List<String> sectorCodes = sectors.stream().map(SectorInfo::getSectorCode).collect(Collectors.toList());
        List<String> sectorNames = sectors.stream().map(SectorInfo::getSectorName).collect(Collectors.toList());

        Map<String, List<StockInfo>> sectorStockDataMap = new HashMap<>();
        for (SectorInfo sector : sectors) {
            String[] stockCodes = sector.getStockCodes().split(",");
            List<StockInfo> allData = new ArrayList<>();
            for (String code : stockCodes) {
                List<StockInfo> data = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                        code.trim(), startDate, endDate);
                allData.addAll(cleanStockData(data));
            }
            sectorStockDataMap.put(sector.getSectorCode(), allData);
        }

        List<LinkageOverview> overviews = new ArrayList<>();
        List<CorrelationMatrixCell> linkageMatrix = new ArrayList<>();
        List<SectorLinkageRank> rankings = new ArrayList<>();

        for (int i = 0; i < sectors.size(); i++) {
            SectorInfo sectorA = sectors.get(i);
            List<StockInfo> dataA = sectorStockDataMap.get(sectorA.getSectorCode());

            double avgVolumeLinkage = 0.0;
            double avgVolatilityLinkage = 0.0;
            int linkageCount = 0;

            for (int j = 0; j < sectors.size(); j++) {
                SectorInfo sectorB = sectors.get(j);
                List<StockInfo> dataB = sectorStockDataMap.get(sectorB.getSectorCode());

                double[] volumeA = calculateSectorDataSeries(dataA, "VOLUME");
                double[] volumeB = calculateSectorDataSeries(dataB, "VOLUME");
                double[] volatilityA = calculateSectorDataSeries(dataA, "VOLATILITY");
                double[] volatilityB = calculateSectorDataSeries(dataB, "VOLATILITY");

                int minLen = Math.min(volumeA.length, volumeB.length);
                double[] vA = Arrays.copyOfRange(volumeA, 0, minLen);
                double[] vB = Arrays.copyOfRange(volumeB, 0, minLen);
                double[] volA = Arrays.copyOfRange(volatilityA, 0, minLen);
                double[] volB = Arrays.copyOfRange(volatilityB, 0, minLen);

                double volumeLinkage = 0.0;
                double volatilityLinkage = 0.0;

                if (minLen > 2) {
                    if ("VOLUME".equals(dataType) || "BOTH".equals(dataType)) {
                        volumeLinkage = pearsonsCorrelation.correlation(vA, vB);
                    }
                    if ("VOLATILITY".equals(dataType) || "BOTH".equals(dataType)) {
                        volatilityLinkage = pearsonsCorrelation.correlation(volA, volB);
                    }
                }

                if (i != j) {
                    avgVolumeLinkage += volumeLinkage;
                    avgVolatilityLinkage += volatilityLinkage;
                    linkageCount++;
                }

                CorrelationMatrixCell cell = new CorrelationMatrixCell();
                cell.setStockCodeX(sectorA.getSectorCode());
                cell.setStockNameX(sectorA.getSectorName());
                cell.setStockCodeY(sectorB.getSectorCode());
                cell.setStockNameY(sectorB.getSectorName());
                cell.setPearsonCoefficient(Math.round(volumeLinkage * 10000.0) / 10000.0);
                cell.setSpearmanCoefficient(Math.round(volatilityLinkage * 10000.0) / 10000.0);
                linkageMatrix.add(cell);
            }

            if (linkageCount > 0) {
                avgVolumeLinkage /= linkageCount;
                avgVolatilityLinkage /= linkageCount;
                double avgLinkage = (avgVolumeLinkage + avgVolatilityLinkage) / 2;

                SectorLinkageRank rank = new SectorLinkageRank();
                rank.setSectorCode(sectorA.getSectorCode());
                rank.setSectorName(sectorA.getSectorName());
                rank.setAvgLinkageCoefficient(Math.round(avgLinkage * 10000.0) / 10000.0);
                rank.setVolumeLinkage(Math.round(avgVolumeLinkage * 10000.0) / 10000.0);
                rank.setVolatilityLinkage(Math.round(avgVolatilityLinkage * 10000.0) / 10000.0);
                rank.setStockCount(sectorA.getStockCount());
                rankings.add(rank);
            }
        }

        rankings.sort((a, b) -> Double.compare(b.getAvgLinkageCoefficient(), a.getAvgLinkageCoefficient()));
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        if (!rankings.isEmpty()) {
            SectorLinkageRank topRank = rankings.get(0);
            LinkageOverview volumeOverview = new LinkageOverview();
            volumeOverview.setIndicatorName("成交量联动");
            volumeOverview.setSectorNameA(topRank.getSectorName());
            volumeOverview.setSectorNameB("市场平均");
            volumeOverview.setLinkageCoefficient(topRank.getVolumeLinkage());
            volumeOverview.setLinkageDescription(getCorrelationDescription(topRank.getVolumeLinkage()));
            overviews.add(volumeOverview);

            LinkageOverview volatilityOverview = new LinkageOverview();
            volatilityOverview.setIndicatorName("波动率联动");
            volatilityOverview.setSectorNameA(topRank.getSectorName());
            volatilityOverview.setSectorNameB("市场平均");
            volatilityOverview.setLinkageCoefficient(topRank.getVolatilityLinkage());
            volatilityOverview.setLinkageDescription(getCorrelationDescription(topRank.getVolatilityLinkage()));
            overviews.add(volatilityOverview);
        }

        List<RollingCorrelationPoint> rollingLinkage = new ArrayList<>();
        if ("ROLLING".equals(request.getCalculationMode()) && rollingWindow != null && sectors.size() >= 2) {
            List<StockInfo> dataA = sectorStockDataMap.get(sectors.get(0).getSectorCode());
            List<StockInfo> dataB = sectorStockDataMap.get(sectors.get(1).getSectorCode());
            rollingLinkage = calculateRollingCorrelation(dataA, dataB, dataType, rollingWindow,
                    sectors.get(0).getSectorCode(), sectors.get(1).getSectorCode(),
                    sectors.get(0).getSectorName(), sectors.get(1).getSectorName(), new ArrayList<>());
        }

        LinkageAnalysisResponse response = new LinkageAnalysisResponse();
        response.setOverviews(overviews);
        response.setSectorRankings(rankings);
        response.setSectorLinkageMatrix(linkageMatrix);
        response.setMatrixSectorCodes(sectorCodes);
        response.setMatrixSectorNames(sectorNames);
        response.setRollingLinkage(rollingLinkage);

        log.info("联动性分析计算完成，排名数: {}, 矩阵单元格数: {}", rankings.size(), linkageMatrix.size());
        return response;
    }

    private List<StockInfo> cleanStockData(List<StockInfo> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        List<StockInfo> cleaned = new ArrayList<>();
        Set<LocalDate> seenDates = new HashSet<>();
        int removedCount = 0;

        for (StockInfo info : data) {
            if (info.getClosePrice() == null || info.getClosePrice() <= 0) {
                removedCount++;
                continue;
            }
            if (info.getVolume() == null || info.getVolume() <= 0) {
                removedCount++;
                continue;
            }
            if (seenDates.contains(info.getTradeDate())) {
                removedCount++;
                continue;
            }
            seenDates.add(info.getTradeDate());
            cleaned.add(info);
        }

        if (removedCount > 0) {
            log.warn("数据清洗完成，移除异常数据 {} 条，剩余有效数据 {} 条", removedCount, cleaned.size());
        }

        return cleaned;
    }

    private double[] extractDataSeries(List<StockInfo> data, String dataType) {
        String type = dataType != null ? dataType.toUpperCase() : "PRICE";
        return switch (type) {
            case "PRICE" -> data.stream().mapToDouble(StockInfo::getClosePrice).toArray();
            case "RETURN" -> data.stream().mapToDouble(s ->
                    s.getDailyReturn() != null ? s.getDailyReturn() : 0.0).toArray();
            case "VOLUME" -> data.stream().mapToDouble(s ->
                    s.getVolume() != null ? s.getVolume().doubleValue() : 0.0).toArray();
            case "VOLATILITY" -> calculateVolatilitySeries(data);
            default -> data.stream().mapToDouble(StockInfo::getClosePrice).toArray();
        };
    }

    private double[] calculateVolatilitySeries(List<StockInfo> data) {
        if (data.size() < 5) {
            return new double[data.size()];
        }

        double[] volatility = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - 19);
            int end = i + 1;
            if (end - start < 5) {
                volatility[i] = 0.0;
            } else {
                List<Double> returns = new ArrayList<>();
                for (int j = start; j < end; j++) {
                    Double ret = data.get(j).getDailyReturn();
                    if (ret != null) {
                        returns.add(ret);
                    }
                }
                if (returns.size() >= 5) {
                    double[] retArray = returns.stream().mapToDouble(Double::doubleValue).toArray();
                    DescriptiveStatistics stats = new DescriptiveStatistics(retArray);
                    volatility[i] = stats.getStandardDeviation();
                } else {
                    volatility[i] = 0.0;
                }
            }
        }
        return volatility;
    }

    private double[] calculateSectorDataSeries(List<StockInfo> data, String dataType) {
        Map<LocalDate, List<StockInfo>> dateGrouped = data.stream()
                .collect(Collectors.groupingBy(StockInfo::getTradeDate));

        List<LocalDate> sortedDates = dateGrouped.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        double[] result = new double[sortedDates.size()];
        for (int i = 0; i < sortedDates.size(); i++) {
            List<StockInfo> dayData = dateGrouped.get(sortedDates.get(i));
            if ("VOLUME".equals(dataType)) {
                result[i] = dayData.stream().mapToLong(StockInfo::getVolume).sum();
            } else if ("VOLATILITY".equals(dataType)) {
                double avgReturn = dayData.stream()
                        .mapToDouble(s -> s.getDailyReturn() != null ? s.getDailyReturn() : 0.0)
                        .average()
                        .orElse(0.0);
                result[i] = avgReturn;
            }
        }
        return result;
    }

    private List<RollingCorrelationPoint> calculateRollingCorrelation(
            List<StockInfo> dataA, List<StockInfo> dataB, String dataType, int windowSize,
            String codeA, String codeB, String nameA, String nameB,
            List<CorrelationDetailRow> detailRows) {

        List<RollingCorrelationPoint> points = new ArrayList<>();

        Map<LocalDate, StockInfo> dataMapA = dataA.stream()
                .collect(Collectors.toMap(StockInfo::getTradeDate, s -> s));
        Map<LocalDate, StockInfo> dataMapB = dataB.stream()
                .collect(Collectors.toMap(StockInfo::getTradeDate, s -> s));

        Set<LocalDate> commonDates = new TreeSet<>(dataMapA.keySet());
        commonDates.retainAll(dataMapB.keySet());
        List<LocalDate> sortedDates = new ArrayList<>(commonDates);

        int serialNumber = 1;

        for (int i = windowSize - 1; i < sortedDates.size(); i++) {
            LocalDate endDate = sortedDates.get(i);
            List<Double> windowA = new ArrayList<>();
            List<Double> windowB = new ArrayList<>();

            for (int j = i - windowSize + 1; j <= i; j++) {
                LocalDate date = sortedDates.get(j);
                StockInfo infoA = dataMapA.get(date);
                StockInfo infoB = dataMapB.get(date);

                if (infoA != null && infoB != null) {
                    double valA = getValueByType(infoA, dataType);
                    double valB = getValueByType(infoB, dataType);
                    windowA.add(valA);
                    windowB.add(valB);
                }
            }

            if (windowA.size() >= windowSize * 0.8) {
                double[] arrA = windowA.stream().mapToDouble(Double::doubleValue).toArray();
                double[] arrB = windowB.stream().mapToDouble(Double::doubleValue).toArray();

                double pearson = 0.0;
                double spearman = 0.0;

                if (arrA.length > 2) {
                    pearson = pearsonsCorrelation.correlation(arrA, arrB);
                    spearman = spearmansCorrelation.correlation(arrA, arrB);
                }

                RollingCorrelationPoint point = new RollingCorrelationPoint();
                point.setTradeDate(endDate);
                point.setWindowSize(windowSize);
                point.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
                point.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
                points.add(point);

                CorrelationDetailRow row = new CorrelationDetailRow();
                row.setSerialNumber(serialNumber++);
                row.setStockCodeA(codeA);
                row.setStockNameA(nameA);
                row.setStockCodeB(codeB);
                row.setStockNameB(nameB);
                row.setStatDate(endDate);
                row.setRollingWindow(windowSize);
                row.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
                row.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
                row.setDataType(dataType);
                detailRows.add(row);

                saveQuantResult("ROLLING_CORRELATION", codeA, nameA, codeB, nameB,
                        sortedDates.get(i - windowSize + 1), endDate, dataType, "BOTH",
                        pearson, spearman, windowSize, endDate);
            }
        }

        return points;
    }

    private void generateDetailRows(List<StockInfo> dataA, List<StockInfo> dataB, String dataType,
                                     String codeA, String codeB, String nameA, String nameB,
                                     List<CorrelationDetailRow> detailRows) {

        Map<LocalDate, StockInfo> dataMapA = dataA.stream()
                .collect(Collectors.toMap(StockInfo::getTradeDate, s -> s));
        Map<LocalDate, StockInfo> dataMapB = dataB.stream()
                .collect(Collectors.toMap(StockInfo::getTradeDate, s -> s));

        Set<LocalDate> commonDates = new TreeSet<>(dataMapA.keySet());
        commonDates.retainAll(dataMapB.keySet());
        List<LocalDate> sortedDates = new ArrayList<>(commonDates);

        double[] allA = new double[sortedDates.size()];
        double[] allB = new double[sortedDates.size()];

        for (int i = 0; i < sortedDates.size(); i++) {
            StockInfo infoA = dataMapA.get(sortedDates.get(i));
            StockInfo infoB = dataMapB.get(sortedDates.get(i));
            allA[i] = getValueByType(infoA, dataType);
            allB[i] = getValueByType(infoB, dataType);
        }

        double pearson = 0.0;
        double spearman = 0.0;
        if (allA.length > 2) {
            pearson = pearsonsCorrelation.correlation(allA, allB);
            spearman = spearmansCorrelation.correlation(allA, allB);
        }

        CorrelationDetailRow row = new CorrelationDetailRow();
        row.setSerialNumber(1);
        row.setStockCodeA(codeA);
        row.setStockNameA(nameA);
        row.setStockCodeB(codeB);
        row.setStockNameB(nameB);
        row.setStatDate(sortedDates.isEmpty() ? LocalDate.now() : sortedDates.get(sortedDates.size() - 1));
        row.setRollingWindow(sortedDates.size());
        row.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
        row.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
        row.setDataType(dataType);
        detailRows.add(row);
    }

    private double getValueByType(StockInfo info, String dataType) {
        return switch (dataType) {
            case "PRICE" -> info.getClosePrice();
            case "RETURN" -> info.getDailyReturn() != null ? info.getDailyReturn() : 0.0;
            case "VOLUME" -> info.getVolume() != null ? info.getVolume().doubleValue() : 0.0;
            case "VOLATILITY" -> info.getDailyReturn() != null ? Math.abs(info.getDailyReturn()) : 0.0;
            default -> info.getClosePrice();
        };
    }

    private String getCorrelationDescription(double coefficient) {
        double abs = Math.abs(coefficient);
        if (coefficient > 0.7) return "强正相关";
        if (coefficient > 0.3) return "弱正相关";
        if (coefficient < -0.7) return "强负相关";
        if (coefficient < -0.3) return "弱负相关";
        return "无相关";
    }

    private void saveQuantResult(String calcType, String codeA, String nameA, String codeB, String nameB,
                                  LocalDate startDate, LocalDate endDate, String dataType, String coeffType,
                                  double pearson, double spearman, Integer window, LocalDate calcDate) {
        QuantCalculationResult result = new QuantCalculationResult();
        result.setCalculationType(calcType);
        result.setStockCodeA(codeA);
        result.setStockNameA(nameA);
        result.setStockCodeB(codeB);
        result.setStockNameB(nameB);
        result.setStatStartDate(startDate);
        result.setStatEndDate(endDate);
        result.setDataType(dataType);
        result.setCoefficientType(coeffType);
        result.setPearsonCoefficient(Math.round(pearson * 10000.0) / 10000.0);
        result.setSpearmanCoefficient(Math.round(spearman * 10000.0) / 10000.0);
        result.setRollingWindow(window);
        result.setCalculationDate(calcDate);
        result.setCorrelationDescription(getCorrelationDescription(pearson));
        quantResultRepository.save(result);
    }

    public List<SectorInfo> getAllSectors() {
        return sectorInfoRepository.findAll();
    }

    public List<Object[]> getAllIndices() {
        return indexDataRepository.findAllDistinctIndices();
    }

    public byte[] exportCorrelationToExcel(List<CorrelationDetailRow> rows) {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("相关性分析明细");

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "标的A代码", "标的A名称", "标的B代码", "标的B名称",
                    "统计日期", "滚动窗口", "Pearson系数", "Spearman系数", "计算数据类型"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (CorrelationDetailRow row : rows) {
                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row.getSerialNumber());
                dataRow.createCell(1).setCellValue(row.getStockCodeA());
                dataRow.createCell(2).setCellValue(row.getStockNameA());
                dataRow.createCell(3).setCellValue(row.getStockCodeB());
                dataRow.createCell(4).setCellValue(row.getStockNameB());
                dataRow.createCell(5).setCellValue(row.getStatDate().toString());
                dataRow.createCell(6).setCellValue(row.getRollingWindow());
                dataRow.createCell(7).setCellValue(row.getPearsonCoefficient());
                dataRow.createCell(8).setCellValue(row.getSpearmanCoefficient());
                dataRow.createCell(9).setCellValue(row.getDataType());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    public byte[] exportLinkageToExcel(List<SectorLinkageRank> rows) {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("板块联动排名");

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"排名", "板块代码", "板块名称", "平均联动系数",
                    "成交量联动", "波动率联动", "成分股数量"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (SectorLinkageRank row : rows) {
                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row.getRank());
                dataRow.createCell(1).setCellValue(row.getSectorCode());
                dataRow.createCell(2).setCellValue(row.getSectorName());
                dataRow.createCell(3).setCellValue(row.getAvgLinkageCoefficient());
                dataRow.createCell(4).setCellValue(row.getVolumeLinkage());
                dataRow.createCell(5).setCellValue(row.getVolatilityLinkage());
                dataRow.createCell(6).setCellValue(row.getStockCount());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败", e);
        }
    }
}
