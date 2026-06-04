package com.stock.analysis.service;

import com.stock.analysis.dto.StockBasicInfo;
import com.stock.analysis.entity.FavoriteStock;
import com.stock.analysis.entity.StockGroup;
import com.stock.analysis.entity.StockInfo;
import com.stock.analysis.repository.FavoriteStockRepository;
import com.stock.analysis.repository.StockGroupRepository;
import com.stock.analysis.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteStockService {

    private final FavoriteStockRepository favoriteStockRepository;
    private final StockGroupRepository stockGroupRepository;
    private final StockInfoRepository stockInfoRepository;

    private StockGroup getOrCreateDefaultGroup(Long userId) {
        return stockGroupRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> {
                    StockGroup defaultGroup = new StockGroup();
                    defaultGroup.setUserId(userId);
                    defaultGroup.setName("默认分组");
                    defaultGroup.setIsDefault(true);
                    return stockGroupRepository.save(defaultGroup);
                });
    }

    public List<StockGroup> getUserGroups(Long userId) {
        getOrCreateDefaultGroup(userId);
        return stockGroupRepository.findByUserIdOrderByCreateTimeAsc(userId);
    }

    @Transactional
    public StockGroup createGroup(Long userId, String name) {
        if (stockGroupRepository.existsByUserIdAndName(userId, name)) {
            throw new RuntimeException("分组名称已存在");
        }
        StockGroup group = new StockGroup();
        group.setUserId(userId);
        group.setName(name);
        group.setIsDefault(false);
        return stockGroupRepository.save(group);
    }

    @Transactional
    public StockGroup updateGroup(Long userId, Long groupId, String name) {
        StockGroup group = stockGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        
        if (!group.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此分组");
        }
        
        if (group.getIsDefault()) {
            throw new RuntimeException("默认分组不能修改名称");
        }
        
        if (!group.getName().equals(name) && stockGroupRepository.existsByUserIdAndName(userId, name)) {
            throw new RuntimeException("分组名称已存在");
        }
        
        group.setName(name);
        return stockGroupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        StockGroup group = stockGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        
        if (!group.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此分组");
        }
        
        if (group.getIsDefault()) {
            throw new RuntimeException("默认分组不能删除");
        }
        
        StockGroup defaultGroup = getOrCreateDefaultGroup(userId);
        
        favoriteStockRepository.updateGroupIdByUserIdAndGroupId(userId, groupId, defaultGroup.getId());
        
        stockGroupRepository.delete(group);
    }

    public List<FavoriteStock> getFavoriteStocks(Long userId, Long groupId) {
        getOrCreateDefaultGroup(userId);
        List<FavoriteStock> favorites;
        if (groupId == null || groupId == 0) {
            favorites = favoriteStockRepository.findByUserIdOrderByAddTimeDesc(userId);
        } else {
            favorites = favoriteStockRepository.findByUserIdAndGroupIdOrderByAddTimeDesc(userId, groupId);
        }
        
        LocalDate latestDate = stockInfoRepository.findAll().stream()
                .map(StockInfo::getTradeDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        for (FavoriteStock favorite : favorites) {
            List<StockInfo> stockInfos = stockInfoRepository.findByCodeAndTradeDateBetweenOrderByTradeDateAsc(
                    favorite.getCode(), latestDate.minusDays(5), latestDate);
            
            if (!stockInfos.isEmpty()) {
                StockInfo latest = stockInfos.get(stockInfos.size() - 1);
                favorite.setPrice(latest.getClosePrice());
                favorite.setName(latest.getName());
                
                if (stockInfos.size() > 1) {
                    StockInfo previous = stockInfos.get(stockInfos.size() - 2);
                    double change = (latest.getClosePrice() - previous.getClosePrice()) / previous.getClosePrice() * 100;
                    favorite.setChangePercent(Math.round(change * 100.0) / 100.0);
                }
            }
        }
        
        return favorites;
    }

    @Transactional
    public List<FavoriteStock> addFavoriteStock(Long userId, String code, Long groupId) {
        StockGroup group = getOrCreateDefaultGroup(userId);
        if (groupId != null && groupId != 0) {
            group = stockGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("分组不存在"));
            if (!group.getUserId().equals(userId)) {
                throw new RuntimeException("无权使用此分组");
            }
        }
        
        Optional<String> stockNameOpt = stockInfoRepository.findNameByCode(code);
        if (stockNameOpt.isEmpty()) {
            throw new RuntimeException("股票代码不存在: " + code);
        }
        
        if (favoriteStockRepository.existsByUserIdAndCode(userId, code)) {
            throw new RuntimeException("该股票已在自选股中: " + code);
        }
        
        FavoriteStock favorite = new FavoriteStock();
        favorite.setUserId(userId);
        favorite.setCode(code);
        favorite.setName(stockNameOpt.get());
        favorite.setGroupId(group.getId());
        favoriteStockRepository.save(favorite);
        
        return getFavoriteStocks(userId, null);
    }

    @Transactional
    public Map<String, Object> addFavoriteStocksBatch(Long userId, List<String> codes, Long groupId) {
        StockGroup group = getOrCreateDefaultGroup(userId);
        if (groupId != null && groupId != 0) {
            group = stockGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("分组不存在"));
            if (!group.getUserId().equals(userId)) {
                throw new RuntimeException("无权使用此分组");
            }
        }
        
        List<String> successCodes = new ArrayList<>();
        List<String> failedCodes = new ArrayList<>();
        List<String> duplicateCodes = new ArrayList<>();
        
        for (String code : codes) {
            code = code.trim();
            if (code.isEmpty()) continue;
            
            try {
                Optional<String> stockNameOpt = stockInfoRepository.findNameByCode(code);
                if (stockNameOpt.isEmpty()) {
                    failedCodes.add(code + " (股票不存在)");
                    continue;
                }
                
                if (favoriteStockRepository.existsByUserIdAndCode(userId, code)) {
                    duplicateCodes.add(code);
                    continue;
                }
                
                FavoriteStock favorite = new FavoriteStock();
                favorite.setUserId(userId);
                favorite.setCode(code);
                favorite.setName(stockNameOpt.get());
                favorite.setGroupId(group.getId());
                favoriteStockRepository.save(favorite);
                successCodes.add(code);
            } catch (Exception e) {
                failedCodes.add(code + " (" + e.getMessage() + ")");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", successCodes);
        result.put("failed", failedCodes);
        result.put("duplicate", duplicateCodes);
        result.put("favorites", getFavoriteStocks(userId, null));
        return result;
    }

    @Transactional
    public List<FavoriteStock> deleteFavoriteStock(Long userId, String code) {
        if (!favoriteStockRepository.existsByUserIdAndCode(userId, code)) {
            throw new RuntimeException("该股票不在自选股中");
        }
        favoriteStockRepository.deleteByUserIdAndCode(userId, code);
        return getFavoriteStocks(userId, null);
    }

    @Transactional
    public List<FavoriteStock> deleteFavoriteStocksBatch(Long userId, List<String> codes) {
        favoriteStockRepository.deleteByUserIdAndCodeIn(userId, codes);
        return getFavoriteStocks(userId, null);
    }

    private List<FavoriteStock> getFavoriteStocksForExport(Long userId, Long groupId, List<String> codes) {
        List<FavoriteStock> allFavorites = getFavoriteStocks(userId, groupId);
        
        if (codes != null && !codes.isEmpty()) {
            Set<String> codeSet = new HashSet<>(codes);
            allFavorites = allFavorites.stream()
                    .filter(fav -> codeSet.contains(fav.getCode()))
                    .collect(Collectors.toList());
        }
        
        return allFavorites;
    }

    public byte[] exportToExcel(Long userId, Long groupId, List<String> codes) throws IOException {
        List<FavoriteStock> favorites = getFavoriteStocksForExport(userId, groupId, codes);
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("自选股");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"股票代码", "股票名称", "当前价格", "涨跌幅(%)", "添加时间"};
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < favorites.size(); i++) {
                FavoriteStock fav = favorites.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(fav.getCode());
                row.createCell(1).setCellValue(fav.getName() != null ? fav.getName() : "");
                row.createCell(2).setCellValue(fav.getPrice() != null ? fav.getPrice() : 0);
                row.createCell(3).setCellValue(fav.getChangePercent() != null ? fav.getChangePercent() : 0);
                row.createCell(4).setCellValue(fav.getAddTime() != null ? fav.getAddTime().format(formatter) : "");
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public String exportToCSV(Long userId, Long groupId, List<String> codes) {
        List<FavoriteStock> favorites = getFavoriteStocksForExport(userId, groupId, codes);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println("股票代码,股票名称,当前价格,涨跌幅(%),添加时间");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (FavoriteStock fav : favorites) {
            pw.printf("%s,%s,%.2f,%.2f,%s%n",
                    fav.getCode(),
                    fav.getName() != null ? fav.getName() : "",
                    fav.getPrice() != null ? fav.getPrice() : 0,
                    fav.getChangePercent() != null ? fav.getChangePercent() : 0,
                    fav.getAddTime() != null ? fav.getAddTime().format(formatter) : ""
            );
        }
        
        return sw.toString();
    }

    public List<StockBasicInfo> searchAvailableStocks(String keyword) {
        List<Object[]> results = stockInfoRepository.searchStocks(keyword);
        return results.stream()
                .map(row -> new StockBasicInfo((String) row[0], (String) row[1]))
                .collect(Collectors.toList());
    }

    public boolean checkStockExists(String code) {
        return stockInfoRepository.findNameByCode(code).isPresent();
    }
}
