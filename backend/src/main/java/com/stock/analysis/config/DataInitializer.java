package com.stock.analysis.config;

import com.stock.analysis.entity.*;
import com.stock.analysis.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StockInfoRepository stockInfoRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final UserDataPermissionRepository dataPermRepository;
    private final OperationLogRepository logRepository;
    private final SystemConfigRepository configRepository;
    private final ScheduledTaskRepository taskRepository;
    private final CollectionBasicInfoRepository collectionBasicInfoRepository;
    private final CollectionMarketDataRepository collectionMarketDataRepository;
    private final CollectionFinancialDataRepository collectionFinancialDataRepository;
    private final CollectionMarketOverviewRepository collectionMarketOverviewRepository;
    private final GovernanceBasicInfoRepository governanceBasicInfoRepository;
    private final GovernanceMarketDataRepository governanceMarketDataRepository;
    private final GovernanceFinancialDataRepository governanceFinancialDataRepository;
    private final GovernanceMarketOverviewRepository governanceMarketOverviewRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final Random random = new Random(42);

    public DataInitializer(StockInfoRepository stockInfoRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           MenuRepository menuRepository,
                           PermissionRepository permissionRepository,
                           UserDataPermissionRepository dataPermRepository,
                           OperationLogRepository logRepository,
                           SystemConfigRepository configRepository,
                           ScheduledTaskRepository taskRepository,
                           CollectionBasicInfoRepository collectionBasicInfoRepository,
                           CollectionMarketDataRepository collectionMarketDataRepository,
                           CollectionFinancialDataRepository collectionFinancialDataRepository,
                           CollectionMarketOverviewRepository collectionMarketOverviewRepository,
                           GovernanceBasicInfoRepository governanceBasicInfoRepository,
                           GovernanceMarketDataRepository governanceMarketDataRepository,
                           GovernanceFinancialDataRepository governanceFinancialDataRepository,
                           GovernanceMarketOverviewRepository governanceMarketOverviewRepository,
                           org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.stockInfoRepository = stockInfoRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
        this.dataPermRepository = dataPermRepository;
        this.logRepository = logRepository;
        this.configRepository = configRepository;
        this.taskRepository = taskRepository;
        this.collectionBasicInfoRepository = collectionBasicInfoRepository;
        this.collectionMarketDataRepository = collectionMarketDataRepository;
        this.collectionFinancialDataRepository = collectionFinancialDataRepository;
        this.collectionMarketOverviewRepository = collectionMarketOverviewRepository;
        this.governanceBasicInfoRepository = governanceBasicInfoRepository;
        this.governanceMarketDataRepository = governanceMarketDataRepository;
        this.governanceFinancialDataRepository = governanceFinancialDataRepository;
        this.governanceMarketOverviewRepository = governanceMarketOverviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initStockData();
        initPermissionData();
        initMenuData();
        initRoleData();
        initUserData();
        initRoleMenusPermissions();
        initDataPermissionData();
        initLogData();
        initConfigData();
        initTaskData();
        initCollectionBasicInfoData();
        initCollectionMarketData();
        initCollectionFinancialData();
        initCollectionMarketOverviewData();
        initGovernanceBasicInfoData();
        initGovernanceMarketData();
        initGovernanceFinancialData();
        initGovernanceMarketOverviewData();
    }

    private void initStockData() {
        if (stockInfoRepository.count() > 0) return;

        String[][] stocks = {
                {"000001", "平安银行"},
                {"000002", "万科A"},
                {"600036", "招商银行"},
                {"600519", "贵州茅台"},
                {"000858", "五粮液"},
                {"601318", "中国平安"},
                {"002594", "比亚迪"},
                {"601899", "紫金矿业"},
                {"002415", "海康威视"},
                {"300750", "宁德时代"}
        };

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        for (String[] stock : stocks) {
            generateStockData(stock[0], stock[1], startDate, endDate);
        }
    }

    private void generateStockData(String code, String name, LocalDate startDate, LocalDate endDate) {
        List<StockInfo> stockInfos = new ArrayList<>();
        double basePrice = getBasePrice(code);
        double currentPrice = basePrice;
        double previousClose = basePrice;

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                double change = (random.nextGaussian() * 0.02) + 0.0005;
                double open = currentPrice * (1 + random.nextGaussian() * 0.005);
                double close = currentPrice * (1 + change);
                double high = Math.max(open, close) * (1 + Math.abs(random.nextGaussian() * 0.01));
                double low = Math.min(open, close) * (1 - Math.abs(random.nextGaussian() * 0.01));
                long volume = (long) (1000000 + random.nextDouble() * 5000000);
                double turnover = volume * ((high + low) / 2);
                double dailyReturn = previousClose != 0 ? (close - previousClose) / previousClose : 0;

                StockInfo info = new StockInfo();
                info.setCode(code);
                info.setName(name);
                info.setTradeDate(currentDate);
                info.setOpenPrice(Math.round(open * 100.0) / 100.0);
                info.setHighPrice(Math.round(high * 100.0) / 100.0);
                info.setLowPrice(Math.round(low * 100.0) / 100.0);
                info.setClosePrice(Math.round(close * 100.0) / 100.0);
                info.setVolume(volume);
                info.setTurnover(Math.round(turnover * 100.0) / 100.0);
                info.setDailyReturn(Math.round(dailyReturn * 10000.0) / 10000.0);

                stockInfos.add(info);

                previousClose = close;
                currentPrice = close;
            }
            currentDate = currentDate.plusDays(1);
        }
        stockInfoRepository.saveAll(stockInfos);
    }

    private double getBasePrice(String code) {
        return switch (code) {
            case "600519" -> 1800.0;
            case "000858" -> 160.0;
            case "601318" -> 45.0;
            case "600036" -> 35.0;
            case "002594" -> 260.0;
            case "300750" -> 200.0;
            case "002415" -> 35.0;
            case "601899" -> 12.0;
            case "000001" -> 12.0;
            case "000002" -> 10.0;
            default -> 50.0;
        };
    }

    private void initPermissionData() {
        if (permissionRepository.count() > 0) return;

        List<Permission> perms = List.of(
            createPerm("查看股票", "stock:view", "查看股票数据", "stock"),
            createPerm("分析股票", "stock:analysis", "进行股票分析", "stock"),
            createPerm("查看用户", "user:view", "查看用户列表", "user"),
            createPerm("新增用户", "user:add", "新增用户", "user"),
            createPerm("编辑用户", "user:edit", "编辑用户", "user"),
            createPerm("删除用户", "user:delete", "删除用户", "user"),
            createPerm("禁用用户", "user:toggle", "禁用/启用用户", "user"),
            createPerm("查看角色", "role:view", "查看角色列表", "role"),
            createPerm("新增角色", "role:add", "新增角色", "role"),
            createPerm("编辑角色", "role:edit", "编辑角色", "role"),
            createPerm("删除角色", "role:delete", "删除角色", "role"),
            createPerm("分配权限", "role:assign", "分配角色权限", "role"),
            createPerm("查看菜单", "menu:view", "查看菜单列表", "menu"),
            createPerm("编辑菜单", "menu:edit", "编辑菜单", "menu"),
            createPerm("查看日志", "log:view", "查看操作日志", "log"),
            createPerm("删除日志", "log:delete", "删除日志", "log"),
            createPerm("查看配置", "config:view", "查看系统配置", "config"),
            createPerm("编辑配置", "config:edit", "编辑系统配置", "config"),
            createPerm("查看任务", "task:view", "查看定时任务", "task"),
            createPerm("编辑任务", "task:edit", "编辑定时任务", "task"),
            createPerm("查看数据权限", "dataperm:view", "查看数据权限", "dataperm"),
            createPerm("编辑数据权限", "dataperm:edit", "编辑数据权限", "dataperm"),
            createPerm("查看基础信息", "collection:basic:view", "查看基础信息采集", "collection"),
            createPerm("查看行情数据", "collection:market:view", "查看行情数据采集", "collection"),
            createPerm("查看财务数据", "collection:financial:view", "查看财务数据采集", "collection"),
            createPerm("查看市场数据", "collection:overview:view", "查看市场数据采集", "collection"),
            createPerm("治理基础信息", "governance:basic:view", "治理基础信息", "governance"),
            createPerm("治理行情数据", "governance:market:view", "治理行情数据", "governance"),
            createPerm("治理财务数据", "governance:financial:view", "治理财务数据", "governance"),
            createPerm("治理市场数据", "governance:overview:view", "治理市场数据", "governance")
        );
        permissionRepository.saveAll(perms);
    }

    private Permission createPerm(String name, String code, String desc, String module) {
        Permission p = new Permission();
        p.setName(name);
        p.setCode(code);
        p.setDescription(desc);
        p.setModule(module);
        return p;
    }

    private void initMenuData() {
        if (menuRepository.count() > 0) return;

        Menu analysisParent = createMenu("数据分析", "/analysis", null, "DataAnalysis", 1, null, "MENU", "stock:view");
        Menu collectionParent = createMenu("数据采集类型", "/collection", null, "Download", 2, null, "MENU", null);
        Menu governanceParent = createMenu("数据治理", "/governance", null, "Edit", 3, null, "MENU", null);
        Menu configParent = createMenu("系统管理", "/system", null, "Tools", 4, null, "MENU", null);
        menuRepository.saveAll(List.of(analysisParent, collectionParent, governanceParent, configParent));

        List<Menu> subMenus = List.of(
            createMenu("股票分析", "/analysis/stock", "analysis/StockAnalysis", "TrendCharts", 1, analysisParent.getId(), "MENU", "stock:view"),
            createMenu("基础信息", "/collection/basic", "collection/BasicInfo", "Info", 1, collectionParent.getId(), "MENU", "collection:basic:view"),
            createMenu("行情数据", "/collection/market", "collection/MarketData", "DataLine", 2, collectionParent.getId(), "MENU", "collection:market:view"),
            createMenu("财务数据", "/collection/financial", "collection/FinancialData", "Data", 3, collectionParent.getId(), "MENU", "collection:financial:view"),
            createMenu("市场数据", "/collection/overview", "collection/MarketOverview", "PieChart", 4, collectionParent.getId(), "MENU", "collection:overview:view"),
            createMenu("基础信息", "/governance/basic", "governance/BasicInfo", "Info", 1, governanceParent.getId(), "MENU", "governance:basic:view"),
            createMenu("行情数据", "/governance/market", "governance/MarketData", "DataLine", 2, governanceParent.getId(), "MENU", "governance:market:view"),
            createMenu("财务数据", "/governance/financial", "governance/FinancialData", "Data", 3, governanceParent.getId(), "MENU", "governance:financial:view"),
            createMenu("市场数据", "/governance/overview", "governance/MarketOverview", "PieChart", 4, governanceParent.getId(), "MENU", "governance:overview:view"),
            createMenu("用户管理", "/system/user", "system/UserManagement", "User", 1, configParent.getId(), "MENU", "user:view"),
            createMenu("角色权限", "/system/role", "system/RolePermission", "UserFilled", 2, configParent.getId(), "MENU", "role:view"),
            createMenu("菜单管理", "/system/menu", "system/MenuManagement", "Menu", 3, configParent.getId(), "MENU", "menu:view"),
            createMenu("操作日志", "/system/log", "system/OperationLog", "Document", 4, configParent.getId(), "MENU", "log:view"),
            createMenu("系统配置", "/system/config", "system/SystemConfig", "Setting", 5, configParent.getId(), "MENU", "config:view"),
            createMenu("定时任务", "/system/task", "system/ScheduledTask", "Timer", 6, configParent.getId(), "MENU", "task:view"),
            createMenu("数据权限", "/system/dataperm", "system/DataPermission", "Lock", 7, configParent.getId(), "MENU", "dataperm:view")
        );
        menuRepository.saveAll(subMenus);
    }

    private Menu createMenu(String name, String path, String component, String icon, int sort, Long parentId, String type, String perm) {
        Menu m = new Menu();
        m.setName(name);
        m.setPath(path);
        m.setComponent(component);
        m.setIcon(icon);
        m.setSortOrder(sort);
        m.setParentId(parentId);
        m.setType(type);
        m.setPermission(perm);
        m.setVisible(true);
        return m;
    }

    private void initRoleData() {
        if (roleRepository.count() > 0) return;

        List<Role> roles = List.of(
            createRole("超级管理员", "ADMIN", "拥有系统所有权限", 1),
            createRole("股票分析师", "ANALYST", "可进行数据分析和查看报告", 2),
            createRole("普通用户", "VIEWER", "只能查看已有数据", 1)
        );
        roleRepository.saveAll(roles);
    }

    private Role createRole(String name, String code, String desc, int userCount) {
        Role role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(desc);
        role.setUserCount(userCount);
        return role;
    }

    private void initUserData() {
        Role adminRole = roleRepository.findByCode("ADMIN").orElse(null);
        Role analystRole = roleRepository.findByCode("ANALYST").orElse(null);
        Role viewerRole = roleRepository.findByCode("VIEWER").orElse(null);

        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setEnabled(true);
            if (adminRole != null) {
                admin.getRoles().clear();
                admin.getRoles().add(adminRole);
            }
            userRepository.save(admin);
        } else {
            User admin = createUser("admin", "系统管理员", "admin@stock.com", "管理员", "123", true, adminRole, "ALL");
            userRepository.save(admin);
        }

        if (userRepository.count() > 1) return;

        List<User> users = List.of(
            createUser("analyst01", "张分析师", "zhang@stock.com", "分析师", "123456", true, analystRole, "ALL"),
            createUser("analyst02", "李研究员", "li@stock.com", "分析师", "123456", true, analystRole, "CUSTOM"),
            createUser("viewer01", "王查看", "wang@stock.com", "访客", "123456", false, viewerRole, "CUSTOM")
        );
        userRepository.saveAll(users);
    }

    private User createUser(String username, String realName, String email, String roleName, String password, boolean enabled, Role role, String dataScope) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone("1380013800" + (random.nextInt(90) + 10));
        user.setRole(roleName);
        user.setEnabled(enabled);
        user.setDataScope(dataScope);
        if (role != null) {
            user.getRoles().add(role);
        }
        return user;
    }

    private void initRoleMenusPermissions() {
        Role adminRole = roleRepository.findByCode("ADMIN").orElse(null);
        Role analystRole = roleRepository.findByCode("ANALYST").orElse(null);
        Role viewerRole = roleRepository.findByCode("VIEWER").orElse(null);

        List<Menu> allMenus = menuRepository.findAll();
        List<Permission> allPerms = permissionRepository.findAll();

        if (adminRole != null) {
            adminRole.setMenus(new HashSet<>(allMenus));
            adminRole.setPermissions(new HashSet<>(allPerms));
            roleRepository.save(adminRole);
        }

        if (analystRole != null && analystRole.getMenus().isEmpty()) {
            Set<Menu> analystMenus = new HashSet<>();
            for (Menu m : allMenus) {
                if ("stock:view".equals(m.getPermission()) || "stock:analysis".equals(m.getPermission())
                        || (m.getPermission() != null && m.getPermission().startsWith("collection:"))
                        || (m.getPermission() != null && m.getPermission().startsWith("governance:"))) {
                    analystMenus.add(m);
                }
            }
            Set<Permission> analystPerms = new HashSet<>();
            for (Permission p : allPerms) {
                if (p.getCode().startsWith("stock:") || p.getCode().startsWith("collection:") || p.getCode().startsWith("governance:")) {
                    analystPerms.add(p);
                }
            }
            analystRole.setMenus(analystMenus);
            analystRole.setPermissions(analystPerms);
            roleRepository.save(analystRole);
        }

        if (viewerRole != null && viewerRole.getMenus().isEmpty()) {
            Set<Menu> viewerMenus = new HashSet<>();
            for (Menu m : allMenus) {
                if ("stock:view".equals(m.getPermission())) {
                    viewerMenus.add(m);
                }
            }
            Set<Permission> viewerPerms = new HashSet<>();
            for (Permission p : allPerms) {
                if ("stock:view".equals(p.getCode())) {
                    viewerPerms.add(p);
                }
            }
            viewerRole.setMenus(viewerMenus);
            viewerRole.setPermissions(viewerPerms);
            roleRepository.save(viewerRole);
        }
    }

    private void initDataPermissionData() {
        if (dataPermRepository.count() > 0) return;

        Optional<User> analyst02 = userRepository.findByUsername("analyst02");
        Optional<User> viewer01 = userRepository.findByUsername("viewer01");

        if (analyst02.isPresent()) {
            UserDataPermission dp = new UserDataPermission();
            dp.setUserId(analyst02.get().getId());
            dp.setAllowedStockCodes("600519,000858,601318");
            dp.setAllowedSectors("白酒,金融");
            dp.setScope("CUSTOM");
            dataPermRepository.save(dp);
        }

        if (viewer01.isPresent()) {
            UserDataPermission dp = new UserDataPermission();
            dp.setUserId(viewer01.get().getId());
            dp.setAllowedStockCodes("000001,000002");
            dp.setAllowedSectors("银行,地产");
            dp.setScope("CUSTOM");
            dataPermRepository.save(dp);
        }
    }

    private void initLogData() {
        if (logRepository.count() > 0) return;

        List<OperationLog> logs = List.of(
            createLog("admin", "用户登录", "192.168.1.100", "POST", "成功", 156),
            createLog("admin", "查看股票列表", "192.168.1.100", "GET", "成功", 45),
            createLog("analyst01", "执行K线分析", "192.168.1.101", "GET", "成功", 234),
            createLog("admin", "新增用户", "192.168.1.100", "POST", "成功", 89),
            createLog("viewer01", "尝试修改配置", "192.168.1.102", "PUT", "失败", 12),
            createLog("admin", "更新系统配置", "192.168.1.100", "PUT", "成功", 67),
            createLog("analyst02", "导出分析报告", "192.168.1.103", "GET", "成功", 1500)
        );
        logRepository.saveAll(logs);
    }

    private OperationLog createLog(String username, String operation, String ip, String method, String status, int cost) {
        OperationLog log = new OperationLog();
        log.setUsername(username);
        log.setOperation(operation);
        log.setIp(ip);
        log.setMethod(method);
        log.setStatus(status);
        log.setCostTime(cost);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }

    private void initConfigData() {
        if (configRepository.count() > 0) return;

        List<SystemConfig> configs = List.of(
            createConfig("system.name", "股票量化分析系统", "系统名称"),
            createConfig("system.version", "1.0.0", "系统版本"),
            createConfig("data.retention.days", "365", "数据保留天数"),
            createConfig("stock.datasource", "mock", "股票数据源类型")
        );
        configRepository.saveAll(configs);
    }

    private SystemConfig createConfig(String key, String value, String desc) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(desc);
        return config;
    }

    private void initTaskData() {
        if (taskRepository.count() > 0) return;

        List<ScheduledTask> tasks = List.of(
            createTask("每日股票数据更新", "STOCK", "0 0 2 * * ?", "每天凌晨2点更新所有股票数据", "运行中"),
            createTask("相关性计算任务", "STOCK", "0 0 3 * * ?", "每天凌晨3点计算股票相关性", "运行中"),
            createTask("波动率指标刷新", "STOCK", "0 */30 * * * ?", "每30分钟刷新波动率指标", "已停止"),
            createTask("日志归档任务", "CLEANUP", "0 0 0 1 * ?", "每月1号归档历史日志", "运行中"),
            createTask("系统健康检查", "SYSTEM", "0 */5 * * * ?", "每5分钟检查系统健康状态", "运行中")
        );
        taskRepository.saveAll(tasks);
    }

    private ScheduledTask createTask(String name, String group, String cron, String desc, String status) {
        ScheduledTask task = new ScheduledTask();
        task.setName(name);
        task.setTaskGroup(group);
        task.setCron(cron);
        task.setDescription(desc);
        task.setStatus(status);
        task.setLastExecuteTime(LocalDateTime.now().minusHours(1));
        return task;
    }

    private void initCollectionBasicInfoData() {
        if (collectionBasicInfoRepository.count() > 0) return;

        List<CollectionBasicInfo> data = List.of(
            createCollectionBasicInfo("000001", "平安银行", "SZ", "银行", "零售金融、跨境金融", 1940591.82, 1940554.63, "1991-04-03"),
            createCollectionBasicInfo("000002", "万科A", "SZ", "房地产", "租售同权、物业管理", 1162537.59, 971517.48, "1991-01-29"),
            createCollectionBasicInfo("600036", "招商银行", "SH", "银行", "零售银行、财富管理", 2521984.57, 2062904.82, "2002-04-09"),
            createCollectionBasicInfo("600519", "贵州茅台", "SH", "食品饮料", "白酒、消费龙头", 125619.78, 125619.78, "2001-08-27"),
            createCollectionBasicInfo("000858", "五粮液", "SZ", "食品饮料", "白酒、国企改革", 388163.84, 388163.84, "1998-04-27"),
            createCollectionBasicInfo("601318", "中国平安", "SH", "非银金融", "保险、金融科技", 1827978.46, 1827978.46, "2007-03-01"),
            createCollectionBasicInfo("002594", "比亚迪", "SZ", "汽车", "新能源汽车、电池", 291114.29, 116447.29, "2011-06-30"),
            createCollectionBasicInfo("601899", "紫金矿业", "SH", "有色金属", "黄金、铜、资源", 263949.67, 263301.67, "2008-04-25"),
            createCollectionBasicInfo("002415", "海康威视", "SZ", "电子", "安防、人工智能", 934492.09, 933092.09, "2010-05-28"),
            createCollectionBasicInfo("300750", "宁德时代", "SZ", "电力设备", "动力电池、储能", 244057.01, 144557.74, "2018-06-11")
        );
        collectionBasicInfoRepository.saveAll(data);
    }

    private CollectionBasicInfo createCollectionBasicInfo(String code, String name, String exchange, String industry,
                                                          String concept, double totalShares, double floatShares, String listDate) {
        CollectionBasicInfo info = new CollectionBasicInfo();
        info.setCode(code);
        info.setName(name);
        info.setExchange(exchange);
        info.setIndustry(industry);
        info.setConcept(concept);
        info.setTotalShares(totalShares);
        info.setFloatShares(floatShares);
        info.setListDate(listDate);
        info.setUpdateTime(LocalDateTime.now());
        return info;
    }

    private void initCollectionMarketData() {
        if (collectionMarketDataRepository.count() > 0) return;

        String[][] stocks = {
            {"000001", "平安银行"}, {"000002", "万科A"}, {"600036", "招商银行"},
            {"600519", "贵州茅台"}, {"000858", "五粮液"}, {"601318", "中国平安"}
        };

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(120);

        for (String[] stock : stocks) {
            generateCollectionMarketData(stock[0], stock[1], startDate, endDate);
        }
    }

    private void generateCollectionMarketData(String code, String name, LocalDate startDate, LocalDate endDate) {
        List<CollectionMarketData> dataList = new ArrayList<>();
        double basePrice = getBasePrice(code);
        double currentPrice = basePrice;
        double previousClose = basePrice;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                double change = (random.nextGaussian() * 0.03) + 0.0005;
                double open = currentPrice * (1 + random.nextGaussian() * 0.008);
                double close = currentPrice * (1 + change);
                double high = Math.max(open, close) * (1 + Math.abs(random.nextGaussian() * 0.015));
                double low = Math.min(open, close) * (1 - Math.abs(random.nextGaussian() * 0.015));
                long volume = (long) (100000 + random.nextDouble() * 5000000);
                double amount = (open + close) / 2 * volume / 10000;
                double turnover = random.nextDouble() * 5;
                double changePercent = (close - previousClose) / previousClose * 100;

                CollectionMarketData data = new CollectionMarketData();
                data.setCode(code);
                data.setName(name);
                data.setTradeDate(current);
                data.setPeriod("daily");
                data.setOpenPrice(Math.round(open * 100.0) / 100.0);
                data.setHighPrice(Math.round(high * 100.0) / 100.0);
                data.setLowPrice(Math.round(low * 100.0) / 100.0);
                data.setClosePrice(Math.round(close * 100.0) / 100.0);
                data.setChangePercent(Math.round(changePercent * 100.0) / 100.0);
                data.setVolume(volume / 100);
                data.setAmount(Math.round(amount * 100.0) / 100.0);
                data.setTurnover(Math.round(turnover * 1000.0) / 1000.0);

                dataList.add(data);
                previousClose = close;
                currentPrice = close;
            }
            current = current.plusDays(1);
        }
        collectionMarketDataRepository.saveAll(dataList);
    }

    private void initCollectionFinancialData() {
        if (collectionFinancialDataRepository.count() > 0) return;

        String[][] stocks = {
            {"000001", "平安银行"}, {"000002", "万科A"}, {"600036", "招商银行"},
            {"600519", "贵州茅台"}, {"000858", "五粮液"}
        };

        String[] periods = {"2025-12-31", "2024-12-31", "2023-12-31", "2022-12-31"};
        String[] reportTypes = {"income", "balance", "cashflow"};

        List<CollectionFinancialData> allData = new ArrayList<>();

        for (int i = 0; i < stocks.length; i++) {
            String code = stocks[i][0];
            String name = stocks[i][1];
            double multiplier = 1 + (i * 0.15);

            for (int j = 0; j < periods.length; j++) {
                double factor = multiplier * (1 - j * 0.08);

                for (String reportType : reportTypes) {
                    CollectionFinancialData data = new CollectionFinancialData();
                    data.setCode(code);
                    data.setName(name);
                    data.setReportPeriod(periods[j]);
                    data.setReportType(reportType);
                    data.setUpdateTime(LocalDateTime.now());

                    if ("income".equals(reportType)) {
                        data.setTotalRevenue(Math.round(12856000L * factor));
                        data.setOperatingCost(Math.round(7235000L * factor));
                        data.setOperatingProfit(Math.round(4528000L * factor));
                        data.setTotalProfit(Math.round(4485000L * factor));
                        data.setNetProfit(Math.round(3528000L * factor));
                        data.setNetProfitParent(Math.round(3425000L * factor));
                        data.setEps(Math.round(4.28 * factor * 100.0) / 100.0);
                        data.setPe(Math.round(28.65 * factor * 100.0) / 100.0);
                        data.setPb(Math.round(3.42 * factor * 100.0) / 100.0);
                        data.setRoe(Math.round(12.35 * factor * 100.0) / 100.0);
                        data.setGrossMargin(Math.round((45.28 + (multiplier - 1) * 5) * 100.0) / 100.0);
                    } else if ("balance".equals(reportType)) {
                        data.setTotalAssets(Math.round(58650000L * factor));
                        data.setTotalLiabilities(Math.round(40080000L * factor * 0.95));
                        data.setTotalEquity(Math.round(18570000L * factor * 1.05));
                        data.setCurrentAssets(Math.round(15680000L * factor));
                        data.setCurrentLiabilities(Math.round(25680000L * factor * 0.95));
                        data.setDebtRatio(Math.round((68.35 - j * 0.5) * 100.0) / 100.0);
                    } else if ("cashflow".equals(reportType)) {
                        data.setOperatingCashFlow(Math.round(5685000L * factor));
                        data.setInvestingCashFlow(Math.round(-1856000L * factor));
                        data.setFinancingCashFlow(Math.round(-856000L * factor));
                        data.setNetCashFlow(Math.round(2973000L * factor));
                    }

                    allData.add(data);
                }
            }
        }

        collectionFinancialDataRepository.saveAll(allData);
    }

    private void initCollectionMarketOverviewData() {
        if (collectionMarketOverviewRepository.count() > 0) return;

        List<CollectionMarketOverview> allData = new ArrayList<>();

        CollectionMarketOverview stats = new CollectionMarketOverview();
        stats.setDataType("MARKET_STATS");
        stats.setTradeDate(LocalDate.now());
        stats.setUpCount(2856);
        stats.setDownCount(1923);
        stats.setFlatCount(321);
        stats.setUpPercent(55.8);
        stats.setDownPercent(37.6);
        stats.setFlatPercent(6.6);
        stats.setNorthFlow(45.68);
        stats.setUpdateTime(LocalDateTime.now());
        allData.add(stats);

        String[][] indices = {
            {"000001", "上证指数"}, {"399001", "深证成指"}, {"399006", "创业板指"},
            {"000688", "科创50"}, {"000300", "沪深300"}, {"000016", "上证50"}
        };

        LocalDate endDate = LocalDate.now();
        for (int i = 30; i >= 1; i--) {
            LocalDate date = endDate.minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                for (String[] idx : indices) {
                    double base = switch (idx[0]) {
                        case "000001" -> 3100;
                        case "399001" -> 10000;
                        case "399006" -> 2000;
                        case "000688" -> 850;
                        case "000300" -> 3500;
                        case "000016" -> 2400;
                        default -> 1000;
                    };
                    double change = (random.nextDouble() - 0.48) * 4;
                    double open = base * (1 + (random.nextDouble() - 0.5) * 0.01);
                    double close = base * (1 + change / 100);
                    double high = Math.max(open, close) * (1 + random.nextDouble() * 0.01);
                    double low = Math.min(open, close) * (1 - random.nextDouble() * 0.01);

                    CollectionMarketOverview data = new CollectionMarketOverview();
                    data.setDataType("INDEX");
                    data.setIndexCode(idx[0]);
                    data.setIndexName(idx[1]);
                    data.setTradeDate(date);
                    data.setOpenPrice(Math.round(open * 100.0) / 100.0);
                    data.setHighPrice(Math.round(high * 100.0) / 100.0);
                    data.setLowPrice(Math.round(low * 100.0) / 100.0);
                    data.setClosePrice(Math.round(close * 100.0) / 100.0);
                    data.setChangePercent(Math.round(change * 100.0) / 100.0);
                    data.setVolume((double) Math.round(200 + random.nextDouble() * 300));
                    data.setAmount((double) Math.round(250 + random.nextDouble() * 400));
                    data.setUpdateTime(LocalDateTime.now());
                    allData.add(data);
                }
            }
        }

        String[][] heatStocks = {
            {"300059", "东方财富"}, {"000725", "京东方A"}, {"600519", "贵州茅台"},
            {"000858", "五粮液"}, {"002594", "比亚迪"}, {"601318", "中国平安"},
            {"600036", "招商银行"}, {"300750", "宁德时代"}, {"000001", "平安银行"},
            {"601899", "紫金矿业"}
        };

        for (String[] stock : heatStocks) {
            CollectionMarketOverview data = new CollectionMarketOverview();
            data.setDataType("HEAT");
            data.setStockCode(stock[0]);
            data.setStockName(stock[1]);
            data.setStockPrice(Math.round((10 + random.nextDouble() * 200) * 100.0) / 100.0);
            data.setStockChange(Math.round((random.nextDouble() - 0.3) * 20 * 100.0) / 100.0);
            data.setStockTurnover(Math.round(random.nextDouble() * 15 * 100.0) / 100.0);
            data.setVolumeRatio(Math.round((0.5 + random.nextDouble() * 3) * 100.0) / 100.0);
            data.setStockAmount((long) (50000 + random.nextDouble() * 500000));
            data.setUpdateTime(LocalDateTime.now());
            allData.add(data);
        }

        for (int i = 29; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                double netFlow = (random.nextDouble() - 0.35) * 100;
                double shBuy = 300 + random.nextDouble() * 200;
                double szBuy = 250 + random.nextDouble() * 150;

                CollectionMarketOverview data = new CollectionMarketOverview();
                data.setDataType("NORTHBOUND");
                data.setTradeDate(date);
                data.setShNetBuy(Math.round(netFlow * 0.6 * 100.0) / 100.0);
                data.setSzNetBuy(Math.round(netFlow * 0.4 * 100.0) / 100.0);
                data.setTotalNetBuy(Math.round(netFlow * 100.0) / 100.0);
                data.setShBuy(Math.round(shBuy * 100.0) / 100.0);
                data.setShSell(Math.round((shBuy - netFlow * 0.6) * 100.0) / 100.0);
                data.setSzBuy(Math.round(szBuy * 100.0) / 100.0);
                data.setSzSell(Math.round((szBuy - netFlow * 0.4) * 100.0) / 100.0);
                data.setUpdateTime(LocalDateTime.now());
                allData.add(data);
            }
        }

        collectionMarketOverviewRepository.saveAll(allData);
    }

    private void initGovernanceBasicInfoData() {
        if (governanceBasicInfoRepository.count() > 0) return;

        List<GovernanceBasicInfo> data = List.of(
            createGovernanceBasicInfo("000001", "平安银行", "SZ", "银行", 1940591.82, "approved", "A", 98, 96),
            createGovernanceBasicInfo("000002", "万科A", "SZ", "房地产", 1162537.59, "pending", "B", 85, 88),
            createGovernanceBasicInfo("600036", "招商银行", "SH", "银行", 2521984.57, "approved", "A", 100, 99),
            createGovernanceBasicInfo("600519", "贵州茅台", "SH", "食品饮料", 125619.78, "need_clean", "C", 70, 75),
            createGovernanceBasicInfo("000858", "五粮液", "SZ", "食品饮料", 388163.84, "approved", "B", 92, 90),
            createGovernanceBasicInfo("601318", "中国平安", "SH", "非银金融", 1827978.46, "rejected", "D", 55, 60),
            createGovernanceBasicInfo("002594", "比亚迪", "SZ", "汽车", 291114.29, "pending", "B", 88, 85),
            createGovernanceBasicInfo("601899", "紫金矿业", "SH", "有色金属", 263949.67, "approved", "A", 95, 94),
            createGovernanceBasicInfo("002415", "海康威视", "SZ", "电子", 934492.09, "need_clean", "C", 72, 78),
            createGovernanceBasicInfo("300750", "宁德时代", "SZ", "电力设备", 244057.01, "approved", "B", 86, 89)
        );
        governanceBasicInfoRepository.saveAll(data);
    }

    private GovernanceBasicInfo createGovernanceBasicInfo(String code, String name, String exchange, String industry,
                                                          double totalShares, String status, String quality,
                                                          int completeness, int accuracy) {
        GovernanceBasicInfo info = new GovernanceBasicInfo();
        info.setCode(code);
        info.setName(name);
        info.setExchange(exchange);
        info.setIndustry(industry);
        info.setTotalShares(totalShares);
        info.setDataStatus(status);
        info.setQualityLevel(quality);
        info.setCompleteness(completeness);
        info.setAccuracy(accuracy);
        info.setUpdateTime(LocalDateTime.now());
        return info;
    }

    private void initGovernanceMarketData() {
        if (governanceMarketDataRepository.count() > 0) return;

        String[][] stocks = {
            {"000001", "平安银行"}, {"000002", "万科A"}, {"600036", "招商银行"},
            {"600519", "贵州茅台"}, {"000858", "五粮液"}, {"601318", "中国平安"}
        };

        List<GovernanceMarketData> allData = new ArrayList<>();
        LocalDate endDate = LocalDate.now();

        for (String[] stock : stocks) {
            double basePrice = getBasePrice(stock[0]);
            double currentPrice = basePrice;

            for (int i = 30; i >= 1; i--) {
                LocalDate date = endDate.minusDays(i);
                int dayOfWeek = date.getDayOfWeek().getValue();
                if (dayOfWeek <= 5) {
                    double change = (random.nextDouble() - 0.48) * 6;
                    double open = currentPrice * (1 + (random.nextDouble() - 0.5) * 0.02);
                    double close = currentPrice * (1 + change / 100);
                    double high = Math.max(open, close) * (1 + random.nextDouble() * 0.015);
                    double low = Math.min(open, close) * (1 - random.nextDouble() * 0.015);
                    long volume = (long) (100000 + random.nextDouble() * 500000);

                    boolean isAbnormal = random.nextDouble() < 0.15;
                    boolean isPending = !isAbnormal && random.nextDouble() < 0.2;

                    GovernanceMarketData data = new GovernanceMarketData();
                    data.setTradeDate(date);
                    data.setCode(stock[0]);
                    data.setName(stock[1]);
                    data.setOpenPrice(Math.round(open * 100.0) / 100.0);
                    data.setHighPrice(Math.round(high * 100.0) / 100.0);
                    data.setLowPrice(Math.round(low * 100.0) / 100.0);
                    data.setClosePrice(Math.round(close * 100.0) / 100.0);
                    data.setChangePercent(Math.round(change * 100.0) / 100.0);
                    data.setVolume(volume);
                    data.setDataStatus(isAbnormal ? "abnormal" : (isPending ? "pending" : "normal"));
                    data.setQualityLevel(isAbnormal ? "D" : (isPending ? "B" : "A"));
                    data.setAbnormalType(isAbnormal ? new String[]{"价格跳空", "成交量异常", "数据缺失"}[random.nextInt(3)] : null);

                    allData.add(data);
                    currentPrice = close;
                }
            }
        }

        governanceMarketDataRepository.saveAll(allData);
    }

    private void initGovernanceFinancialData() {
        if (governanceFinancialDataRepository.count() > 0) return;

        String[][] stocks = {
            {"000001", "平安银行"}, {"000002", "万科A"}, {"600036", "招商银行"},
            {"600519", "贵州茅台"}, {"000858", "五粮液"}
        };

        String[] periods = {"2024Q1", "2023年报", "2023Q3", "2023Q2", "2023Q1", "2022年报"};
        String[] reportTypes = {"income", "balance", "cashflow"};

        List<GovernanceFinancialData> allData = new ArrayList<>();

        for (int i = 0; i < stocks.length; i++) {
            String code = stocks[i][0];
            String name = stocks[i][1];

            for (int j = 0; j < periods.length; j++) {
                boolean isPending = random.nextDouble() < 0.2;
                boolean isAbnormal = !isPending && random.nextDouble() < 0.1;
                boolean isFormatError = !isPending && !isAbnormal && random.nextDouble() < 0.1;

                for (String reportType : reportTypes) {
                    GovernanceFinancialData data = new GovernanceFinancialData();
                    data.setPeriod(periods[j]);
                    data.setCode(code);
                    data.setName(name);
                    data.setReportType(reportType);
                    data.setItem1((long) Math.floor((random.nextDouble() * 500 + 300) * 100000000L));
                    data.setItem2((long) Math.floor((random.nextDouble() * 100 + 50) * 100000000L));
                    data.setItem3((long) Math.floor((random.nextDouble() * 200 + 150) * 100000000L));
                    data.setItem4((long) Math.floor((random.nextDouble() * 50 + 30) * 100000000L));
                    data.setItem5((long) Math.floor((random.nextDouble() * 30 + 20) * 100000000L));
                    data.setDataStatus(isPending ? "pending" : (isAbnormal ? "abnormal" : (isFormatError ? "format_error" : "synced")));
                    data.setVerifyStatus(isAbnormal ? "fail" : (random.nextDouble() < 0.8 ? "pass" : "uncheck"));
                    data.setUpdateTime(LocalDateTime.now());
                    allData.add(data);
                }
            }
        }

        governanceFinancialDataRepository.saveAll(allData);
    }

    private void initGovernanceMarketOverviewData() {
        if (governanceMarketOverviewRepository.count() > 0) return;

        List<GovernanceMarketOverview> allData = new ArrayList<>();

        String[][] indices = {
            {"000001", "上证指数"}, {"399001", "深证成指"}, {"399006", "创业板指"}
        };

        LocalDate endDate = LocalDate.now();
        for (int i = 30; i >= 1; i--) {
            LocalDate date = endDate.minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                for (String[] idx : indices) {
                    double base = switch (idx[0]) {
                        case "000001" -> 3100;
                        case "399001" -> 10000;
                        case "399006" -> 2000;
                        default -> 1000;
                    };
                    double change = (random.nextDouble() - 0.48) * 4;
                    double open = base * (1 + (random.nextDouble() - 0.5) * 0.01);
                    double close = base * (1 + change / 100);
                    double high = Math.max(open, close) * (1 + random.nextDouble() * 0.01);
                    double low = Math.min(open, close) * (1 - random.nextDouble() * 0.01);

                    boolean isPending = random.nextDouble() < 0.15;
                    boolean isAbnormal = !isPending && random.nextDouble() < 0.1;
                    boolean isDuplicate = !isPending && !isAbnormal && random.nextDouble() < 0.05;

                    GovernanceMarketOverview data = new GovernanceMarketOverview();
                    data.setDataType("INDEX");
                    data.setTradeDate(date);
                    data.setIndexCode(idx[0]);
                    data.setIndexName(idx[1]);
                    data.setOpenPrice(Math.round(open * 100.0) / 100.0);
                    data.setHighPrice(Math.round(high * 100.0) / 100.0);
                    data.setLowPrice(Math.round(low * 100.0) / 100.0);
                    data.setClosePrice(Math.round(close * 100.0) / 100.0);
                    data.setChangePercent(Math.round(change * 100.0) / 100.0);
                    data.setVolume((double) Math.round(200 + random.nextDouble() * 300));
                    data.setDataStatus(isPending ? "pending" : (isAbnormal ? "abnormal" : (isDuplicate ? "duplicate" : "normal")));
                    allData.add(data);
                }
            }
        }

        for (int i = 14; i >= 1; i--) {
            LocalDate date = endDate.minusDays(i);
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek <= 5) {
                double shNetBuy = Math.round((random.nextDouble() - 0.4) * 50 * 100.0) / 100.0;
                double szNetBuy = Math.round((random.nextDouble() - 0.4) * 40 * 100.0) / 100.0;

                GovernanceMarketOverview data = new GovernanceMarketOverview();
                data.setDataType("NORTHBOUND");
                data.setTradeDate(date);
                data.setShNetBuy(shNetBuy);
                data.setSzNetBuy(szNetBuy);
                data.setTotalNetBuy(Math.round((shNetBuy + szNetBuy) * 100.0) / 100.0);
                data.setShBuy(Math.round((300 + random.nextDouble() * 200) * 100.0) / 100.0);
                data.setShSell(Math.round((280 + random.nextDouble() * 180) * 100.0) / 100.0);
                data.setSzBuy(Math.round((250 + random.nextDouble() * 150) * 100.0) / 100.0);
                data.setSzSell(Math.round((230 + random.nextDouble() * 140) * 100.0) / 100.0);
                data.setDataStatus(random.nextDouble() < 0.9 ? "normal" : "pending");
                allData.add(data);
            }
        }

        governanceMarketOverviewRepository.saveAll(allData);
    }
}