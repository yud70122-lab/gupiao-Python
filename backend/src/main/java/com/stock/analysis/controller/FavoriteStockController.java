package com.stock.analysis.controller;

import com.stock.analysis.dto.StockBasicInfo;
import com.stock.analysis.entity.FavoriteStock;
import com.stock.analysis.entity.StockGroup;
import com.stock.analysis.service.FavoriteStockService;
import com.stock.analysis.service.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteStockController {

    private final FavoriteStockService favoriteStockService;

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            List<StockGroup> groups = favoriteStockService.getUserGroups(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "分组名称不能为空"));
            }
            StockGroup group = favoriteStockService.createGroup(userId, name.trim());
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/groups/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable Long groupId, @RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "分组名称不能为空"));
            }
            StockGroup group = favoriteStockService.updateGroup(userId, groupId, name.trim());
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            favoriteStockService.deleteGroup(userId, groupId);
            List<StockGroup> groups = favoriteStockService.getUserGroups(userId);
            return ResponseEntity.ok(Map.of("message", "删除成功", "groups", groups));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stocks")
    public ResponseEntity<?> getFavoriteStocks(@RequestParam(required = false) Long groupId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            List<FavoriteStock> favorites = favoriteStockService.getFavoriteStocks(userId, groupId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/stocks")
    public ResponseEntity<?> addFavoriteStock(@RequestBody Map<String, Object> request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            String code = (String) request.get("code");
            Long groupId = request.get("groupId") != null ? ((Number) request.get("groupId")).longValue() : null;
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "股票代码不能为空"));
            }
            
            List<FavoriteStock> favorites = favoriteStockService.addFavoriteStock(userId, code.trim(), groupId);
            return ResponseEntity.ok(Map.of("message", "添加成功", "favorites", favorites));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/stocks/batch")
    public ResponseEntity<?> addFavoriteStocksBatch(@RequestBody Map<String, Object> request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) request.get("codes");
            Long groupId = request.get("groupId") != null ? ((Number) request.get("groupId")).longValue() : null;
            
            if (codes == null || codes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "股票代码列表不能为空"));
            }
            
            Map<String, Object> result = favoriteStockService.addFavoriteStocksBatch(userId, codes, groupId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/stocks/{code}")
    public ResponseEntity<?> deleteFavoriteStock(@PathVariable String code) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            List<FavoriteStock> favorites = favoriteStockService.deleteFavoriteStock(userId, code);
            return ResponseEntity.ok(Map.of("message", "删除成功", "favorites", favorites));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/stocks/batch")
    public ResponseEntity<?> deleteFavoriteStocksBatch(@RequestBody Map<String, List<String>> request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            List<String> codes = request.get("codes");
            if (codes == null || codes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "股票代码列表不能为空"));
            }
            
            List<FavoriteStock> favorites = favoriteStockService.deleteFavoriteStocksBatch(userId, codes);
            return ResponseEntity.ok(Map.of("message", "删除成功", "favorites", favorites));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam(required = false) Long groupId,
                                                 @RequestParam(required = false) String codes) throws IOException {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<String> codeList = (codes != null && !codes.isEmpty()) 
                ? java.util.Arrays.asList(codes.split(",")) 
                : null;
        byte[] excelData = favoriteStockService.exportToExcel(userId, groupId, codeList);
        String fileName = "自选股_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCSV(@RequestParam(required = false) Long groupId,
                                              @RequestParam(required = false) String codes) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        
        List<String> codeList = (codes != null && !codes.isEmpty()) 
                ? java.util.Arrays.asList(codes.split(",")) 
                : null;
        String csvData = favoriteStockService.exportToCSV(userId, groupId, codeList);
        String fileName = "自选股_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/stocks/search")
    public ResponseEntity<?> searchAvailableStocks(@RequestParam String keyword) {
        try {
            List<StockBasicInfo> stocks = favoriteStockService.searchAvailableStocks(keyword);
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stocks/check/{code}")
    public ResponseEntity<?> checkStockExists(@PathVariable String code) {
        try {
            boolean exists = favoriteStockService.checkStockExists(code);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
