# 股票量化关系数据分析与可视化系统 —— AI 开发提示词指南

## 文档说明

本文档为完整的 AI 开发提示词集合，按照顺序依次执行每条提示词，即可得到一个完整可用的股票量化分析系统。每条提示词均包含：页面操作说明、展示内容要求、后端接口定义、数据库表设计及验收标准。提示词已改写为自然语言，可直接复制粘贴交给 AI 开发工具使用。

**技术栈**：Spring Boot 3.2.0 + JPA/Hibernate + H2 内存数据库 + Spring Security + JWT + Vue 2.7 + Vuex + Vue Router + Element UI + ECharts 5 + Axios + Vite 4

**项目工作区**：`d:\gitHab-p\新建文件夹\gupiao-Python`

**提示词总计**：14 步，覆盖系统全部 37 张数据库表、约 170 个接口、40+ 个前端页面

---

> **重要提示**：本系统使用 H2 内存数据库（create-drop 模式），服务重启后所有数据将重置。请在完成所有开发后，将数据库切换为 MySQL 以持久化数据。虚拟交易模块的数据同样依赖 H2，重启后交易记录会丢失。

---

## 提示词第 1 步：项目环境初始化与基础框架搭建

请帮我完成项目的初始化工作。项目位于 `d:\gitHab-p\新建文件夹\gupiao-Python`，采用前后端分离架构。

**后端部分**：

后端使用 Spring Boot 3.2.0 + Java 17 + Maven。请在 `backend` 目录下完成以下工作：

1. 创建 `pom.xml`，引入以下核心依赖：
   - `spring-boot-starter-web`：Web MVC 框架
   - `spring-boot-starter-data-jpa`：JPA/Hibernate ORM
   - `com.h2database:h2`：H2 内存数据库（runtime scope）
   - `spring-boot-starter-security`：Spring Security 认证授权
   - `spring-boot-starter-aop`：切面编程，用于权限注解
   - `spring-boot-starter-validation`：数据校验
   - `io.jsonwebtoken:jjwt-api:0.12.5` / `jjwt-impl` / `jjwt-jackson`：JWT Token
   - `org.apache.commons:commons-math3:3.6.1`：统计计算（Pearson/Spearman 相关系数）
   - `org.projectlombok:lombok`：代码简化
   - `org.apache.poi:poi:5.2.5` / `poi-ooxml`：Excel 导入导出
   - `spring-boot-starter-data-redis`：Redis 集成（可选，先引入不报错即可）
   - `spring-boot-starter-test`：测试框架

2. 创建 `src/main/resources/application.properties`，配置：
   - 服务端口：8008
   - H2 内存数据库：`jdbc:h2:mem:stockdb`，使用 create-drop 模式（每次重启重建）
   - H2 Web 控制台启用（路径 `/h2-console`）
   - JPA 自动建表策略：`ddl-auto=update`
   - 文件上传最大 10MB
   - 日志级别：INFO

3. 创建 Spring Boot 启动类 `com.stock.analysis.StockAnalysisApplication`

4. 创建 `SecurityConfig` 配置类，要求：
   - 禁用 CSRF
   - 允许所有来源的 CORS 跨域请求，允许所有方法、所有头部，允许携带凭据
   - 会话策略设为 STATELESS（无状态，使用 JWT）
   - 公开端点：`/api/auth/**`、`/h2-console/**`、`/error`
   - 其他所有接口都需要认证
   - 使用 BCryptPasswordEncoder 作为密码编码器
   - 注册 JWT 认证过滤器，在 UsernamePasswordAuthenticationFilter 之前执行

5. 创建 `JwtUtil` 工具类，实现 JWT Token 的生成、解析、校验，使用 HMAC-SHA256 签名，密钥配置在 properties 中，默认过期时间 24 小时

6. 创建 `JwtAuthenticationFilter` 过滤器，从请求头 `Authorization: Bearer <token>` 提取 Token，验证后加载用户信息到 SecurityContext

7. 创建统一返回格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": 1718900000000
}
```
  其中 code 含义：200 成功、401 未登录、403 权限不足、400 参数错误、500 服务器异常

8. 创建全局异常处理器，统一处理各类异常并返回统一格式

**前端部分**：

前端使用 Vue 2.7 + Vite 4 + Vue Router 3 + Vuex 3 + Element UI + ECharts 5 + Axios。请在 `frontend` 目录下完成：

1. 初始化项目（若已有 `package.json` 则检查依赖完整性），确保以下依赖已安装：
   - `vue@2.7.16`、`vue-router@3.6.5`、`vuex@3.6.2`
   - `element-ui@2.15.14`
   - `echarts@5.4.3`
   - `axios@1.6.0`
   - `vite@4.5.0`、`vite-plugin-vue2@2.0.3`、`vue-template-compiler@2.7.16`

2. 配置 `vite.config.js`：
   - 前端开发服务器端口 3008
   - 配置代理：`/api` 代理到 `http://localhost:8008`
   - 路径别名：`@` 指向 `src` 目录

3. 创建 Axios 封装（`src/api/index.js`）：
   - baseURL 设为 `/api`
   - 请求拦截器：自动从 localStorage 读取 token 并附加到请求头 `Authorization: Bearer <token>`
   - 响应拦截器：自动解包 `response.data`，当返回 401 时清除 token 并跳转登录页

4. 创建 Vuex Store（`src/store/index.js`），State 包含：token、userInfo、permissions、menus；所有数据通过 localStorage 持久化；Actions 包含 login 和 logout

5. 创建路由配置（`src/router/index.js`），初始只配两个路由：登录页 `/login` 和首页 `/`；添加路由守卫：未登录跳登录页

6. 创建 `App.vue` 根组件和 `main.js` 入口文件

7. 创建 `index.html` 入口 HTML

**验收标准**：
- 后端能正常启动在 8008 端口，H2 控制台可访问
- 前端能正常启动在 3008 端口，代理转发正常工作
- 访问受保护接口返回 401，公开接口正常

---

## 提示词第 2 步：数据库建模与初始数据构建

请继续在当前项目基础上完成数据库建模和数据初始化工作。

**后端 - 创建 Entity 实体类**（放在 `com.stock.analysis.entity` 包中）：

1. **User（用户表）**：id(Long)、username、password、realName、email、phone、role(String，如 ADMIN/ANALYST/VIEWER)、enabled(Boolean)、dataScope(String，如 ALL/SECTOR/STOCK)、lastLoginIp、createTime、updateTime

2. **Role（角色表）**：id(Long)、name、code、description、userCount(Integer)、createTime

3. **Permission（权限表）**：id(Long)、name、code、description、module(String，所属模块)

4. **Menu（菜单表）**：id(Long)、name、path、component、icon、sortOrder(Integer)、parentId(Long)、type(String，DIRECTORY/MENU/BUTTON)、permission(String，权限标识)、visible(Boolean)、createTime

5. 创建中间表关联关系：User 与 Role 多对多、Role 与 Menu 多对多、Role 与 Permission 多对多

6. **UserDataPermission（用户数据权限表）**：id(Long)、userId(Long)、allowedStockCodes(String)、allowedSectors(String)、scope(String)

7. **SystemConfig（系统配置表）**：id(Long)、configKey(String，唯一)、configValue(String)、description、createTime、updateTime

8. **OperationLog（操作日志表）**：id(Long)、username、operation、ip、method、params(String，LONGTEXT)、status(Integer)、costTime(Long)、createTime

9. **StockInfo（股票日行情数据表）**：id(Long)、code(String)、name、tradeDate(LocalDate)、open(Double)、high、low、close、volume(Long)、amount(Double)、dailyReturn(Double，日收益率)

10. **IndexData（指数数据表）**：id(Long)、indexCode、indexName、tradeDate、open、high、low、close、volume、dailyReturn

11. **SectorInfo（板块信息表）**：id(Long)、sectorCode(String)、sectorName、sectorType(String，行业/概念)、stockCodes(String，LONGTEXT，逗号分隔的股票代码)、stockCount(Integer)

12. **StockGroup（自选股分组表）**：id(Long)、userId(Long)、name、isDefault(Boolean)、createTime

13. **FavoriteStock（自选股表）**：id(Long)、userId(Long)、code、name、groupId(Long)、price(Double)、changePercent(Double)、createTime

14. **QuantCalculationResult（量化计算结果表）**：id(Long)、calculationType(String)、stockCodeA、stockCodeB、pearsonCoefficient(Double)、spearmanCoefficient(Double)、rollingWindow(Integer)、calcTime(LocalDateTime)

15. **ScheduledTask（定时任务表）**：id(Long)、name、taskGroup、cron、targetMethod、status(String，RUNNING/STOPPED/PAUSED)、lastExecuteTime、createTime

16. 数据采集相关的 4 张表（CollectionBasicInfo、CollectionMarketData、CollectionFinancialData、CollectionMarketOverview）

17. 数据治理相关的 4 张表（GovernanceBasicInfo、GovernanceMarketData、GovernanceFinancialData、GovernanceMarketOverview）

每张表需要对应的 JPA Repository 接口。

**后端 - 创建 DataInitializer**（`com.stock.analysis.config.DataInitializer`）：

实现 `CommandLineRunner` 接口，在应用启动时自动初始化模拟数据：

1. 创建 35 个权限定义（覆盖所有模块的 view/edit/delete/export 等操作）
2. 创建 6 个父菜单 + 22 个子菜单，构建完整的菜单树
3. 创建 3 个角色：ADMIN（超级管理员，拥有全部权限）、ANALYST（分析师）、VIEWER（普通用户）
4. 创建 4 个预设用户：admin（密码 123456）、analyst01、analyst02、viewer01（密码均为 123456），使用 BCrypt 加密
5. 创建 10 只股票的两年日 K 线模拟数据（约 5000 条），涵盖不同的行业和板块
6. 创建 7 个指数的模拟数据
7. 创建 12 个板块数据
8. 创建采集侧和治理侧的对等模拟数据（治理侧部分数据标记为异常/待审核状态）
9. 创建 5 个定时任务定义、4 个系统配置项、7 条操作日志

**验收标准**：
- 启动后端后 H2 数据库中能查到所有初始化的数据
- 使用 admin/123456 能正常登录
- 菜单树结构完整

---

## 提示词第 3 步：用户认证与权限管理模块

请帮我完成用户认证和完整的权限管理功能。

### 页面需构建或完善的内容

#### 1. 登录页（/login）
- 页面展示：系统 Logo 和标题"股票量化分析系统"居中显示；用户名输入框（带用户图标前缀）；密码输入框（带锁图标前缀）；"记住密码"复选框；登录按钮（蓝色，全宽）；注册链接（"还没有账号？立即注册"）
- 操作：输入用户名和密码后点击登录，调用登录接口获取 JWT Token，存入 localStorage 并跳转到首页；登录失败显示错误提示（如"用户名或密码错误"）；支持按 Enter 键提交登录
- 若已登录访问 /login 自动跳转到首页

#### 2. 注册页
- 页面展示：用户名、真实姓名、邮箱、手机号、密码、确认密码输入框；注册按钮
- 操作：前端校验两次密码一致、密码长度 >= 6 位；提交注册接口；注册成功提示并跳转登录页

#### 3. 个人中心（/profile）
- 页面展示：用户头像（默认头像）、用户名、角色标签、邮箱、手机号；信息编辑表单；修改密码区域（原密码、新密码、确认新密码）
- 操作：编辑个人资料、修改密码

#### 4. 用户管理页（/system/user）
- 页面展示：用户列表表格（列：用户名、真实姓名、邮箱、角色、状态、创建时间、操作）；搜索栏（支持按用户名/真实姓名搜索）；新增/编辑弹窗（含所有用户字段表单）；角色分配下拉选择
- 操作：分页查询用户列表、新增用户、编辑用户信息、启用/禁用用户、删除用户、分页切换

#### 5. 角色管理页（/system/role）
- 页面展示：角色列表表格（列：角色名称、角色编码、描述、用户数、创建时间、操作）；权限分配弹窗（树形复选框展示所有权限，按模块分组）；菜单分配弹窗（树形复选框展示所有菜单）
- 操作：新增/编辑/删除角色、为角色分配权限（勾选保存）、为角色分配菜单（勾选保存）

#### 6. 菜单管理页（/system/menu）
- 页面展示：菜单树形表格（支持展开/折叠）；列：菜单名称、图标、排序号、路径、组件路径、权限标识、类型、操作
- 操作：新增顶级菜单或子菜单、编辑菜单、删除菜单（级联删除子菜单）

#### 7. 权限管理页（/system/permission）
- 页面展示：权限列表，按模块分组的折叠面板展示，每组显示权限名称和编码
- 操作：新增/编辑/删除权限

#### 8. 数据权限管理页（/system/dataperm）
- 页面展示：用户列表及每个用户的数据权限配置；股票代码多选输入框（逗号分隔）；板块多选下拉框
- 操作：为用户配置可访问的股票代码范围和板块范围

### 后端接口

**认证相关（AuthController，路径 /api/auth，全部公开）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 登录，参数{username, password}，返回{token, userInfo} |
| /api/auth/register | POST | 注册，参数{username, password, realName, email, phone} |
| /api/auth/logout | POST | 退出登录（清除服务端会话） |
| /api/auth/info | GET | 获取当前用户信息（含权限列表、菜单列表） |
| /api/auth/update-profile | PUT | 修改个人资料 |
| /api/auth/change-password | PUT | 修改密码，参数{oldPassword, newPassword} |
| /api/auth/reset-password | POST | 重置密码（管理员操作），参数{userId, newPassword} |

**用户管理（UserController，路径 /api/users，需认证）：**

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /api/users | GET | user:view | 分页查询用户列表，参数{pageNum, pageSize, keyword} |
| /api/users/{id} | GET | user:view | 查询单个用户详情 |
| /api/users | POST | user:create | 新增用户 |
| /api/users/{id} | PUT | user:edit | 编辑用户 |
| /api/users/{id} | DELETE | user:delete | 删除用户 |
| /api/users/{id}/status | PUT | user:edit | 启用/禁用用户，参数{enabled} |
| /api/users/{id}/roles | PUT | user:assign-role | 分配角色，参数{roleIds} |

**角色管理（RoleController，路径 /api/roles，需认证）：**

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /api/roles | GET | role:view | 查询所有角色列表 |
| /api/roles/{id} | GET | role:view | 查询单个角色详情（含权限和菜单ID列表） |
| /api/roles | POST | role:create | 新增角色 |
| /api/roles/{id} | PUT | role:edit | 编辑角色 |
| /api/roles/{id} | DELETE | role:delete | 删除角色（已有用户关联时禁止删除） |
| /api/roles/{id}/permissions | PUT | role:assign | 分配权限，参数{permissionIds} |
| /api/roles/{id}/menus | PUT | role:assign | 分配菜单，参数{menuIds} |

**菜单管理（MenuController，路径 /api/menus，需认证）：**

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /api/menus | GET | menu:view | 查询菜单树（按 parentId 层级返回） |
| /api/menus/{id} | GET | menu:view | 查询单个菜单 |
| /api/menus | POST | menu:create | 新增菜单 |
| /api/menus/{id} | PUT | menu:edit | 编辑菜单 |
| /api/menus/{id} | DELETE | menu:delete | 删除菜单（级联删除子菜单） |

**权限管理（PermissionController，路径 /api/permissions，需认证）：**

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /api/permissions | GET | role:view | 查询所有权限（按模块分组） |
| /api/permissions/{id} | GET | role:view | 查询单个权限 |
| /api/permissions | POST | role:create | 新增权限 |
| /api/permissions/{id} | PUT | role:edit | 编辑权限 |
| /api/permissions/{id} | DELETE | role:delete | 删除权限 |

**数据权限管理（DataPermissionController，路径 /api/data-permissions，需认证）：**

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /api/data-permissions | GET | dataperm:view | 查询所有用户的数据权限列表 |
| /api/data-permissions/{userId} | GET | dataperm:view | 查询指定用户的数据权限 |
| /api/data-permissions/{userId} | PUT | dataperm:edit | 设置用户数据权限，参数{allowedStockCodes, allowedSectors, scope} |

**关键实现要求**：
- 角色管理接口需实现 @RequirePermission 自定义注解 + AOP 切面进行权限校验
- 用户登录互斥：同一账号只能在一个地方登录（后登录的踢掉先登录的），通过 IP 前缀对比实现异地登录检测
- 密码必须 BCrypt 加密存储，禁止明文

**验收标准**：
- 所有页面渲染正常，表格数据加载正确
- 登录注册流程完整可用
- 角色分配权限后，对应权限的用户只能看到有权限的菜单
- 接口权限校验生效：无权限用户调用接口返回 403

---

## 提示词第 4 步：股票数据采集与治理模块

请帮我完成股票数据的采集管理和数据治理功能。

### 页面需构建的内容

#### 1. 数据采集 - 基础信息页（/collection/basic）
- 页面展示：股票基础信息列表表格（列：股票代码、名称、交易所、行业、概念板块、总股本、操作）；搜索栏（支持按代码/名称/行业筛选）；"触发采集"按钮
- 操作：分页查询、按条件筛选、点击"触发采集"启动数据采集任务、查看详情弹窗

#### 2. 数据采集 - 行情数据页（/collection/market）
- 页面展示：行情数据列表表格（列：股票代码、名称、交易日期、周期、开盘价、收盘价、最高价、最低价、成交量、数据状态）；搜索栏（按代码/日期范围筛选）；"触发采集"按钮
- 操作：分页查询、按条件筛选、触发采集

#### 3. 数据采集 - 财务数据页（/collection/financial）
- 页面展示：财务数据列表表格（列：股票代码、报告期、报告类型、PE、PB、ROE、EPS、操作）；搜索栏
- 操作：分页查询、筛选、触发采集

#### 4. 数据采集 - 市场概览页（/collection/overview）
- 页面展示：市场概览数据显示（指数行情、市场热度、北向资金流入流出、涨跌统计）；数据表格（列：数据类型、交易日期、指数代码/名称、开收盘价、净买入额）；"触发采集"按钮
- 操作：分页查询、筛选

#### 5. 数据治理 - 基础信息页（/governance/basic）
- 页面展示：治理基础信息列表表格（列：股票代码、名称、数据状态（已审核/待审核/已驳回/需清洗）、质量等级（A/B/C/D）、完整度百分比、准确度百分比、操作列（审核通过/驳回/标记清洗））
- 操作：审核通过、驳回（填写驳回原因）、标记为需清洗、批量审核

#### 6. 数据治理 - 行情数据页（/governance/market）
- 页面展示：行情数据列表表格（列：交易日期、股票代码、开高低收、数据状态、质量等级、异常类型）；"数据修复"按钮；"数据校验"按钮
- 操作：数据审核、异常标记与修复、数据校验（检测缺失值/异常值）

#### 7. 数据治理 - 财务数据页（/governance/financial）
- 页面展示：财务数据列表表格（列：报告期、股票代码、报告类型、关键财务指标、数据状态、校验状态）；"勾稽校验"按钮（检查资产负债表平衡）
- 操作：审核、标准化处理、勾稽校验

#### 8. 数据治理 - 市场概览页（/governance/overview）
- 页面展示：市场概览治理数据（含指数、北向资金、市场统计的治理状态）；操作列
- 操作：审核、修复、标准化

#### 9. 数据统计 - 数据状态页（/statistics/status）
- 页面展示：各类型数据总量统计卡片（采集侧和治理侧各 4 张卡片，显示总数、最后更新时间、覆盖率）；数据覆盖率进度条
- 操作：查看统计详情

#### 10. 数据统计 - 采集日志页（/statistics/logs）
- 页面展示：采集日志列表表格（列：数据类型、股票代码、名称、状态（成功/失败）、错误信息、重试次数、采集时间）；"重试"按钮
- 操作：分页查询、按类型/状态筛选、手动重试失败的采集任务

### 后端接口

**数据采集（CollectionController，路径 /api/collection，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/collection/basic | GET | 分页查询基础信息，参数{pageNum, pageSize, keyword, industry} |
| /api/collection/basic/{id} | GET | 查询单条详情 |
| /api/collection/basic/collect | POST | 触发基础信息采集，参数{stockCodes} |
| /api/collection/market | GET | 分页查询行情数据，参数{pageNum, pageSize, code, startDate, endDate} |
| /api/collection/market/{id} | GET | 查询单条详情 |
| /api/collection/market/collect | POST | 触发行情采集 |
| /api/collection/financial | GET | 分页查询财务数据 |
| /api/collection/financial/{id} | GET | 查询单条详情 |
| /api/collection/financial/collect | POST | 触发财务数据采集 |
| /api/collection/overview | GET | 分页查询市场概览 |
| /api/collection/overview/collect | POST | 触发市场概览采集 |

**数据治理（GovernanceController，路径 /api/governance，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/governance/basic | GET | 分页查询治理基础信息，参数{dataStatus, qualityLevel} |
| /api/governance/basic/{id}/approve | PUT | 审核通过 |
| /api/governance/basic/{id}/reject | PUT | 驳回，参数{reason} |
| /api/governance/basic/{id}/mark-clean | PUT | 标记需清洗 |
| /api/governance/basic/batch-approve | PUT | 批量审核，参数{ids} |
| /api/governance/market | GET | 分页查询治理行情数据 |
| /api/governance/market/{id}/approve | PUT | 审核通过 |
| /api/governance/market/{id}/repair | PUT | 数据修复 |
| /api/governance/market/{id}/validate | PUT | 数据校验 |
| /api/governance/market/batch-validate | PUT | 批量校验 |
| /api/governance/financial | GET | 分页查询治理财务数据 |
| /api/governance/financial/{id}/approve | PUT | 审核通过 |
| /api/governance/financial/{id}/standardize | PUT | 标准化处理 |
| /api/governance/financial/{id}/cross-validate | PUT | 勾稽校验 |
| /api/governance/overview | GET | 分页查询治理市场概览 |
| /api/governance/overview/{id}/approve | PUT | 审核通过 |
| /api/governance/overview/{id}/repair | PUT | 数据修复 |

**数据统计（StatisticsController，路径 /api/statistics，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/statistics/status | GET | 查询各类型数据总量和覆盖率 |
| /api/statistics/logs | GET | 分页查询采集日志 |
| /api/statistics/logs/{id}/retry | POST | 重试失败采集 |
| /api/statistics/logs/clear | DELETE | 清空所有日志 |

**验收标准**：
- 4 个采集页面和 4 个治理页面数据正常展示
- 采集触发按钮点击后有反馈提示
- 治理审核操作后数据状态正确切换

---

## 提示词第 5 步：股票行情分析与 K 线可视化模块

请帮我完善股票行情数据分析页面，实现从后端获取真实数据的可视化分析。

### 页面需要改进的内容

#### 股票分析主页面（/analysis/stock）

**搜索与股票选择区域**：
- 展示：股票搜索输入框（支持按代码/名称模糊搜索，下拉显示匹配结果）；已选股票标签列表（可点击 x 移除）
- 操作：输入关键词后调用搜索接口，选择股票后添加到对比列表，最多选择 5 只股票同时对比

**K 线图展示区域**（使用 ECharts K 线图 + 副图）：
- 展示：主图展示日 K 线（蜡烛图），红色上涨绿色下跌（中国习惯）；副图分别为 MACD（DIF/DEA/MACD柱）、KDJ（K/D/J 三条线）、RSI（RSI6/RSI12/RSI24）、BOLL 布林带（上轨/中轨/下轨 + K 线）
- 操作：点击切换主图股票；K 线图支持缩放、平移、十字光标悬浮显示 OHLC 数据；点击副图指标可切换显示/隐藏；支持时间区间拖拽选择

**多股票对比曲线区域**：
- 展示：多股票归一化价格对比折线图（将起始价格归一化为 1）；多股票收益率对比折线图
- 操作：悬停显示具体数值；支持时间区间筛选（快捷选项：近1月/3月/6月/1年/2年）

**相关性热力图区域**：
- 展示：所选股票两两之间的 Pearson 相关性系数矩阵热力图；矩阵单元格内显示具体相关系数数值
- 操作：悬停显示详细数值

**风险收益散点图区域**：
- 展示：横轴为年化波动率（风险），纵轴为年化收益率（收益），每个点代表一只股票；点的大小代表夏普比率
- 操作：悬停显示详细指标（收益率、波动率、夏普比率、最大回撤）

**右侧指标面板**：
- 展示：当前选中股票的实时指标卡片（累计收益率、年化收益率、年化波动率、夏普比率、最大回撤）

### 后端接口

**股票数据（StockController，路径 /api/stocks，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/stocks/search | GET | 股票搜索，参数{keyword}，返回[{code, name, exchange, industry}] |
| /api/stocks/kline | GET | K 线数据查询，参数{codes(逗号分隔,最多5个), startDate, endDate} |
| /api/stocks/returns | GET | 收益分析，参数{codes, startDate, endDate} |
| /api/stocks/volatility | GET | 波动率分析，参数{codes, startDate, endDate}，返回{年化波动率,夏普比率,最大回撤} |
| /api/stocks/correlation-matrix | GET | 相关性矩阵，参数{codes, method(pearson/spearman), startDate, endDate} |
| /api/stocks/risk-return | GET | 风险收益数据，参数{codes, startDate, endDate} |
| /api/stocks/{code}/info | GET | 查询单只股票基本信息 |

**关键计算逻辑**：
- Pearson 相关系数使用 Apache Commons Math3
- 年化收益率 = 日均收益率 * 252
- 年化波动率 = 日收益率标准差 * sqrt(252)
- 夏普比率 = (年化收益率 - 无风险利率3%) / 年化波动率
- 最大回撤 = max((峰值 - 谷值) / 峰值)

**验收标准**：
- K 线图使用真实数据渲染，技术指标计算正确
- 多股票对比曲线平滑显示
- 热力图和散点图数据正确

---

## 提示词第 6 步：量化关系计算引擎模块

请帮我完成量化关系计算引擎的功能，包括相关性分析、联动性分析和因子分析。

### 页面需构建的内容

#### 1. 相关性分析页（/quant/correlation）

**操作区**：股票选择器（多选，至少选2只，最多20只）；相关性方法选择（Pearson/Spearman）；分析维度选择（价格相关性/收益率相关性）；滚动窗口设置（可选，20/60/120日）；"开始分析"按钮

**结果展示区**：相关性概览卡片（最强正相关/负相关股票对、平均相关性）；相关性矩阵热力图；相关性详细列表；滚动相关性趋势折线图

#### 2. 联动性分析页（/quant/linkage）

**操作区**：板块选择下拉框；联动性类型选择（成交量联动/波动率联动/收益率联动）；时间区间选择；"开始分析"按钮

**结果展示区**：板块联动排名表格；联动矩阵热力图；板块内股票关联网络图（节点大小代表市值，连线粗细代表相关性，支持拖拽）

#### 3. 因子分析页（/quant/factor）

**操作区**：因子类型选择（价量因子/动量因子/成长因子/财务因子/资金因子）；时间区间；"开始计算"按钮

**结果展示区**：因子数值排名表格；因子分布直方图；因子 IC/IR 分析；因子有效性评估

### 后端接口

**量化分析（QuantAnalysisController，路径 /api/quant，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/quant/correlation | POST | 相关性分析，参数{stockCodes[], method, analysisType, startDate, endDate} |
| /api/quant/correlation/rolling | POST | 滚动相关性，参数{stockCodeA, stockCodeB, method, window, startDate, endDate} |
| /api/quant/correlation/export | POST | 导出结果为 Excel |
| /api/quant/linkage/sector-rank | POST | 板块联动排名 |
| /api/quant/linkage/matrix | POST | 板块联动矩阵 |
| /api/quant/linkage/network | POST | 股票关联网络数据 |
| /api/quant/linkage/export | POST | 导出联动分析结果 |
| /api/quant/factor/calculate | POST | 因子计算 |
| /api/quant/factor/ic-analysis | POST | IC/IR分析 |
| /api/quant/factor/export | POST | 导出因子分析结果 |
| /api/quant/sectors | GET | 查询所有板块列表 |
| /api/quant/indexes | GET | 查询所有指数列表 |

**验收标准**：
- 相关性矩阵计算准确
- 网络图交互流畅，支持拖拽缩放
- 滚动相关性趋势图正确展示时间变化

---

## 提示词第 7 步：量化策略回测模块

请帮我实现量化策略回测模块。

### 页面需构建的内容

#### 1. 策略模板库页（/strategy/templates）
- 展示：策略模板卡片网格布局，内置 6 个策略模板：配对交易策略、双均线策略、布林带策略、行业轮动策略、多因子选股策略、北向资金跟随策略
- 操作：点击查看策略详情（含参数说明、适用场景）；点击"使用此策略"进入回测配置

#### 2. 回测配置与执行页（/strategy/backtest）
- 操作区：策略模板选择、回测时间区间、初始资金（默认100万）、手续费率、印花税率、滑点、持仓上限、复权模式选择、自定义策略参数
- 结果展示区："开始回测"按钮（异步执行，显示进度条）；回测完成后展示：
  - 收益指标：累计收益率、年化收益率、超额收益率
  - 风险指标：最大回撤、年化波动率、夏普比率、索提诺比率、卡玛比率
  - 交易指标：胜率、盈亏比、最大连续亏损、总交易次数
  - 资产曲线图：累计净值曲线 vs 基准指数曲线，标注回撤阴影区域
  - 月度收益热力图
  - 交易明细表格
  - 持仓分布饼图

#### 3. 策略管理页（/strategy/manage）
- 展示：策略列表表格；新建/编辑/复制/删除策略；查看回测记录

#### 4. 回测记录页（/strategy/records）
- 展示：历史回测记录列表；策略对比（勾选2-5条记录，并排展示收益曲线、回撤曲线、月度收益对比）

### 后端接口

**策略回测（BacktestController，路径 /api/backtest，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/backtest/strategies | GET | 查询策略模板列表 |
| /api/backtest/strategies/{id} | GET | 查询策略模板详情 |
| /api/backtest/strategies | POST | 新建自定义策略 |
| /api/backtest/strategies/{id} | PUT | 编辑策略 |
| /api/backtest/strategies/{id} | DELETE | 删除策略 |
| /api/backtest/execute | POST | 执行回测（异步），返回回测结果 ID |
| /api/backtest/result/{id} | GET | 查询回测结果详情 |
| /api/backtest/result/{id}/curve | GET | 查询资产曲线数据 |
| /api/backtest/result/{id}/monthly-returns | GET | 查询月度收益热力图数据 |
| /api/backtest/result/{id}/trades | GET | 分页查询交易明细 |
| /api/backtest/records | GET | 查询历史回测记录列表 |
| /api/backtest/records/{id} | DELETE | 删除回测记录 |
| /api/backtest/compare | POST | 策略对比，参数{recordIds[]} |
| /api/backtest/records/{id}/export | GET | 导出回测结果 Excel |

**数据库新增表**：
- **strategy_info**：id、strategyName、strategyType、params(JSON)、description、userId、createTime
- **backtest_record**：id、strategyId、userId、startDate、endDate、initCapital、totalReturn、annualReturn、sharpeRatio、maxDrawdown、winRate、totalTrades、resultDetail(JSON)、createTime
- **backtest_trade**：id、recordId、stockCode、tradeType、price、quantity、amount、commission、profitLoss、tradeTime

**验收标准**：
- 6个策略模板均可正常回测
- 回测指标计算准确
- 资产曲线图和数据展示正确

---

## 提示词第 8 步：虚拟交易模块（核心新增模块，使用 H2 内存数据库）

这是本次扩展的**核心新增模块**。请帮我实现一个完整的虚拟交易系统，使用项目已有的 H2 内存数据库存储所有交易数据。虚拟交易模块让用户可以在模拟环境中买卖股票，跟踪持仓和盈亏。

### 页面需构建的内容

#### 1. 虚拟交易首页 - 账户总览（/trading/dashboard）

**顶部账户摘要栏**：
- 展示：总资产（大号数字，绿色）、可用资金、持仓市值、累计盈亏（红色亏绿色盈，含百分比）、今日盈亏
- 展示：账户名称、"充值"按钮、"创建新账户"按钮、账户切换下拉框

**资产变化折线图**：
- 展示：总资产随时间变化的折线图（横轴日期，纵轴金额）；叠加显示基准指数涨跌百分比
- 操作：支持时间区间切换（近1周/近1月/近3月/全部）

**持仓列表表格**：
- 展示：表格列：股票代码、名称、持仓数量、平均成本价、当前价格、持仓市值、盈亏金额、盈亏百分比（红绿显示）、占总资产比例（进度条）
- 操作：点击股票行弹出买入/卖出操作弹窗；支持按盈亏排序

**最近交易记录**：
- 展示：最近 10 条交易记录列表（列：时间、股票、方向标签(买入绿色/卖出红色)、价格、数量、金额、手续费）
- 操作：点击"查看全部"跳转到交易记录页

#### 2. 买入股票页（/trading/buy）

**操作区（左侧表单）**：
- 展示：股票搜索选择器（输入代码/名称模糊搜索）；实时显示当前价格、涨跌幅、成交量；买入数量输入框；"最大可买"快捷按钮（根据可用资金和当前价格自动计算）；预估金额显示；预估手续费显示；资金变动预览
- 操作：输入数量后实时更新预估数据；点击"确认买入"提交订单；下单前弹窗确认

#### 3. 卖出股票页（/trading/sell）

**操作区（左侧表单）**：
- 展示：持仓股票选择下拉框（只显示当前持仓的股票）；实时显示当前价格和持仓盈亏；可卖数量显示；卖出数量输入框；"全部卖出"快捷按钮；预估到账金额
- 操作：选择股票后自动填充可卖数量；点击"确认卖出"提交订单

#### 4. 交易记录页（/trading/history）

**筛选栏**：股票代码/名称搜索；交易方向筛选（全部/买入/卖出）；时间区间选择器；"导出 Excel"按钮

**交易记录表格**：列：交易时间、股票代码、名称、方向标签、成交价格、数量、金额、手续费、盈亏金额、累计盈亏

**交易统计**：总交易次数、买入/卖出次数、总手续费、总盈利/亏损金额、净盈亏、胜率；月度交易量柱状图

#### 5. 盈亏分析页（/trading/pnl）

**盈亏摘要卡片行**：已实现盈亏、未实现盈亏（持仓浮动盈亏）、总盈亏、收益率

**盈亏分布饼图**：按股票分布的盈亏饼图

**持仓明细盈亏表**：列：股票代码、名称、持仓数量、平均成本、当前价、持仓市值、浮动盈亏、浮动盈亏率、持仓天数

**历史盈亏走势图**：每日累计已实现盈亏折线图；每日浮动盈亏折线图（虚线）

#### 6. 账户管理页（/trading/accounts）

**账户列表**：每个账户的卡片（显示账户名称、初始资金、当前总资产、收益率、创建时间）；"新建账户"按钮；"充值/提现"按钮
- 操作：新建虚拟账户（填写名称和初始资金）；切换活跃账户；充值虚拟资金；删除账户

### 后端接口

**虚拟交易（TradingController，路径 /api/trading，需认证）：**

账户管理：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/trading/accounts | GET | 查询当前用户的所有虚拟账户列表 |
| /api/trading/accounts | POST | 创建新虚拟账户，参数{accountName, initialCapital}，默认初始资金100万 |
| /api/trading/accounts/{id} | GET | 查询账户详情（含总资产、可用资金、持仓市值、累计盈亏） |
| /api/trading/accounts/{id} | DELETE | 删除虚拟账户（清空所有关联数据） |
| /api/trading/accounts/{id}/deposit | POST | 充值虚拟资金，参数{amount} |
| /api/trading/accounts/{id}/withdraw | POST | 提取虚拟资金（不能超过可用资金），参数{amount} |
| /api/trading/accounts/{id}/assets-history | GET | 查询资产历史变化数据，参数{startDate, endDate} |

持仓管理：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/trading/accounts/{accountId}/positions | GET | 查询当前持仓列表（含实时行情） |
| /api/trading/accounts/{accountId}/positions/{stockCode} | GET | 查询某只股票的持仓详情 |

交易下单：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/trading/accounts/{accountId}/buy | POST | 买入股票，参数{stockCode, quantity}；返回订单ID |
| /api/trading/accounts/{accountId}/sell | POST | 卖出股票，参数{stockCode, quantity}；返回订单ID |

交易查询：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/trading/accounts/{accountId}/transactions | GET | 分页查询交易记录，参数{pageNum, pageSize, stockCode, tradeType, startDate, endDate} |
| /api/trading/accounts/{accountId}/transactions/export | GET | 导出交易记录为 Excel |
| /api/trading/accounts/{accountId}/pnl | GET | 查询盈亏分析数据，返回{realizedPnl, unrealizedPnl, totalPnl, returnRate, pnlByStock[], dailyPnlHistory[]} |

行情查询（供交易使用）：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/trading/stocks/{code}/realtime | GET | 查询股票实时行情（最新价、涨跌幅、成交量） |

### 数据库表设计（全部使用 H2 内存数据库）

**virtual_account（虚拟账户表）**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| account_name | VARCHAR(100) | 账户名称 |
| initial_capital | DECIMAL(18,2) | 初始资金 |
| available_cash | DECIMAL(18,2) | 可用资金 |
| frozen_cash | DECIMAL(18,2) | 冻结资金（挂单占用） |
| total_assets | DECIMAL(18,2) | 总资产（资金+持仓市值） |
| total_profit_loss | DECIMAL(18,2) | 累计盈亏 |
| total_profit_loss_pct | DECIMAL(10,4) | 累计盈亏百分比 |
| status | VARCHAR(20) | 状态：ACTIVE |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

**virtual_position（虚拟持仓表）**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | BIGINT | 账户ID |
| stock_code | VARCHAR(20) | 股票代码 |
| stock_name | VARCHAR(100) | 股票名称 |
| total_quantity | INTEGER | 总持仓数量 |
| available_quantity | INTEGER | 可用数量 |
| avg_cost | DECIMAL(10,4) | 平均成本价 |
| current_price | DECIMAL(10,4) | 当前价格 |
| market_value | DECIMAL(18,2) | 持仓市值 |
| unrealized_pnl | DECIMAL(18,2) | 浮动盈亏 |
| unrealized_pnl_pct | DECIMAL(10,4) | 浮动盈亏百分比 |
| created_at | TIMESTAMP | 首次建仓时间 |
| updated_at | TIMESTAMP | 最后更新时间 |

**virtual_order（虚拟订单表）**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | BIGINT | 账户ID |
| user_id | BIGINT | 用户ID |
| stock_code | VARCHAR(20) | 股票代码 |
| stock_name | VARCHAR(100) | 股票名称 |
| order_type | VARCHAR(10) | BUY/SELL |
| order_price | DECIMAL(10,4) | 委托价格 |
| order_quantity | INTEGER | 委托数量 |
| filled_quantity | INTEGER | 已成交数量 |
| filled_price | DECIMAL(10,4) | 成交均价 |
| status | VARCHAR(20) | PENDING/FILLED/CANCELLED |
| created_at | TIMESTAMP | 下单时间 |
| updated_at | TIMESTAMP | 更新时间 |

**virtual_transaction（虚拟成交记录表）**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | BIGINT | 账户ID |
| order_id | BIGINT | 关联订单ID |
| stock_code | VARCHAR(20) | 股票代码 |
| stock_name | VARCHAR(100) | 股票名称 |
| trade_type | VARCHAR(10) | BUY/SELL |
| trade_price | DECIMAL(10,4) | 成交价格 |
| trade_quantity | INTEGER | 成交数量 |
| trade_amount | DECIMAL(18,2) | 成交金额 |
| commission | DECIMAL(18,2) | 手续费 |
| stamp_tax | DECIMAL(18,2) | 印花税（卖出时） |
| profit_loss | DECIMAL(18,2) | 本次卖出盈亏（买入时为null） |
| trade_time | TIMESTAMP | 成交时间 |
| created_at | TIMESTAMP | 记录创建时间 |

**virtual_asset_history（资产历史记录表）**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| account_id | BIGINT | 账户ID |
| record_date | DATE | 记录日期 |
| total_assets | DECIMAL(18,2) | 当日总资产 |
| available_cash | DECIMAL(18,2) | 当日可用资金 |
| market_value | DECIMAL(18,2) | 当日持仓市值 |
| realized_pnl | DECIMAL(18,2) | 当日已实现盈亏 |
| unrealized_pnl | DECIMAL(18,2) | 当日未实现盈亏 |

### JPA Repository 接口

创建以下 Repository（继承 JpaRepository）：
- `VirtualAccountRepository`
- `VirtualPositionRepository`
- `VirtualOrderRepository`
- `VirtualTransactionRepository`
- `VirtualAssetHistoryRepository`

### Service 层业务逻辑（TradingService）

1. **createAccount(userId, name, initialCapital)**：创建虚拟账户，初始化 availableCash = initialCapital
2. **buyStock(accountId, stockCode, quantity, price)**：
   - 校验可用资金（数量*价格 + 手续费0.03%）
   - 创建订单 → 立即成交 → 状态 FILLED
   - 扣减可用资金；若已有该股票持仓则更新（重算平均成本价），否则创建新持仓
   - 记录成交明细；更新账户总资产
3. **sellStock(accountId, stockCode, quantity, price)**：
   - 校验持仓数量足够
   - 创建订单 → 成交
   - 盈亏 = (卖出价 - 平均成本) * 数量 - 手续费0.03% - 印花税0.1%
   - 更新持仓（清零则删除记录）；增加可用资金；更新账户累计盈亏
4. **getAccountSummary(accountId)**：汇总总资产、收益率
5. **updateAssetSnapshot(accountId)**：每日收盘记录资产快照

### 前端路由配置

| 路径 | 组件 | 说明 |
|------|------|------|
| /trading/dashboard | TradingDashboard.vue | 交易首页（账户总览） |
| /trading/buy | TradingBuy.vue | 买入股票 |
| /trading/sell | TradingSell.vue | 卖出股票 |
| /trading/history | TradingHistory.vue | 交易记录 |
| /trading/pnl | TradingPnl.vue | 盈亏分析 |
| /trading/accounts | TradingAccounts.vue | 账户管理 |

在 `src/api/` 下新建 `tradingApi.js`，封装以上所有后端接口。

### 验证规则
- 买入：金额 >= 100元，数量 >= 100股（A 股整手规则）
- 卖出：数量 <= 持仓可用数量
- 提现：金额 <= 可用资金
- 账户名：不为空，长度 2-50 字符

**验收标准**：
- 能创建虚拟账户并充值
- 买入股票后持仓列表正确显示
- 卖出股票后资金正确到账（扣除手续费和印花税）
- 盈亏计算准确
- 交易记录完整可查
- 资产历史曲线正确反映交易变化

---

## 提示词第 9 步：实时行情与 WebSocket 推送模块

请帮我实现实时行情推送功能，使用 WebSocket 实现服务端向客户端的行情数据推送。

### 页面需构建的内容

#### 1. 实时行情页（/market/realtime）

**分时图区域**：选中股票的分时走势图（当日每分钟价格折线 + 均价线）；行情数据卡片（最新价、涨跌额、涨跌幅、开盘价、最高价、最低价）

**盘口数据区域**：五档买卖盘（卖一到卖五：价格和挂单量，红色；买一到买五：价格和挂单量，绿色）

**行情排行区域**：Tab切换（涨幅榜/跌幅榜/成交额榜/换手率榜），每榜Top 20股票

#### 2. 市场监控页（/market/monitor）

**大盘指数实时面板**：上证指数、深证成指、创业板指的实时走势迷你图 + 当前点数 + 涨跌幅；北向资金净流入实时展示；涨跌家数统计

**板块资金流向**：板块净流入TOP 10 横向柱状图；板块涨跌幅排行榜

### 后端接口

**WebSocket 推送配置**：
- 使用 Spring WebSocket 实现
- 连接端点：`/ws`
- 订阅主题：`/topic/market/{stockCode}`（单股票）、`/topic/market/ranking`（排行）、`/topic/market/overview`（大盘概览）
- 服务端每3秒读取模拟的实时行情数据推送到对应主题

**REST 接口（MarketController，路径 /api/market，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/market/realtime/{code} | GET | 查询单只股票实时行情 |
| /api/market/ranking | GET | 查询行情排行，参数{type, limit(默认20)} |
| /api/market/overview | GET | 查询大盘概览 |
| /api/market/sectors-flow | GET | 查询板块资金流向 Top 10 |

**验收标准**：
- 打开实时行情页后价格自动更新
- WebSocket 推送延迟控制在 500ms 以内
- 断线后能自动重连

---

## 提示词第 10 步：智能预警与消息通知模块

请帮我实现智能预警和消息通知功能。

### 页面需构建的内容

#### 1. 预警规则管理页（/alert/rules）
- 操作：新增预警（弹出配置弹窗：预警类型选择、股票选择、触发条件设置、阈值输入、通知方式、有效期）
- 展示：预警规则列表表格（预警类型标签、股票、触发条件、通知方式、启用/停用开关、操作）
- 操作：编辑、删除、启停规则

#### 2. 预警记录页（/alert/records）
- 展示：预警记录列表（触发时间、类型、股票、触发条件、实际数值、状态标签）
- 操作：标记已读、删除、查看详情

#### 3. 消息通知页（/alert/messages）
- 展示：站内信列表（类型图标、标题、摘要、时间、已读/未读状态）；"全部已读"按钮；导航栏消息图标带未读角标
- 操作：点击消息查看详情、标记已读、删除

### 后端接口

**预警管理（AlertController，路径 /api/alerts，需认证）：**

预警规则：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/alerts/rules | GET | 分页查询预警规则列表 |
| /api/alerts/rules | POST | 新增预警规则 |
| /api/alerts/rules/{id} | PUT | 编辑规则 |
| /api/alerts/rules/{id} | DELETE | 删除规则 |
| /api/alerts/rules/{id}/toggle | PUT | 启用/停用 |

预警记录：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/alerts/records | GET | 分页查询触发记录 |
| /api/alerts/records/{id}/read | PUT | 标记已读 |

消息通知：
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/alerts/messages | GET | 分页查询站内信 |
| /api/alerts/messages/unread-count | GET | 查询未读数量 |
| /api/alerts/messages/{id}/read | PUT | 标记已读 |
| /api/alerts/messages/read-all | PUT | 全部已读 |

**数据库新增表**：
- **warning_rule**：id、userId、ruleType、stockCode、condition(JSON)、threshold、notifyWays、enabled、expireDate、createTime
- **warning_record**：id、ruleId、userId、stockCode、triggerValue、triggerTime、status
- **notification_message**：id、userId、title、content、type、relatedId、isRead、createTime

**预警触发逻辑（@Scheduled 每5分钟扫描）**：
- 价格预警：对比当前价格与阈值
- 指标预警：计算 MACD/RSI/成交量并判断条件
- 量化关系预警：计算相关系数并判断阈值

**验收标准**：
- 能创建不同类型预警规则并正常触发
- 站内信列表正确展示，未读角标准确

---

## 提示词第 11 步：批量任务与分布式调度模块

请帮我实现批量任务调度功能。

### 页面需构建的内容

#### 1. 批量任务页（/tasks/batch）
- 展示：3种任务类型卡片（全市场相关性扫描、全市场因子排名、批量策略回测）
- 操作：配置参数后提交任务；查看任务队列（状态标签、进度条、预计剩余时间）；暂停/取消/重启任务

#### 2. 任务管理页（/tasks/manage）
- 展示：定时任务列表（任务名称、Cron表达式、状态、上次/下次执行时间）
- 操作：启动/停止/暂停定时任务、立即执行、编辑Cron、查看执行历史

#### 3. 定时任务配置页（/system/task）
- 若已有，增加"立即执行"按钮和查看执行历史功能

### 后端接口

**批量任务（TaskController，路径 /api/tasks，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/tasks/batch/submit | POST | 提交批量任务，参数{taskType, params} |
| /api/tasks/batch/list | GET | 分页查询任务列表 |
| /api/tasks/batch/{id} | GET | 查询任务详情和进度 |
| /api/tasks/batch/{id}/pause | POST | 暂停任务 |
| /api/tasks/batch/{id}/resume | POST | 恢复任务 |
| /api/tasks/batch/{id}/cancel | POST | 取消任务 |
| /api/tasks/batch/{id}/result | GET | 查询任务结果 |
| /api/tasks/scheduled | GET | 查询定时任务列表 |
| /api/tasks/scheduled/{id} | PUT | 编辑定时任务 |
| /api/tasks/scheduled/{id}/start | POST | 启动 |
| /api/tasks/scheduled/{id}/stop | POST | 停止 |
| /api/tasks/scheduled/{id}/execute | POST | 立即执行 |

**验收标准**：
- 批量任务能正常提交并异步执行
- 进度条实时更新
- 定时任务按Cron准时执行

---

## 提示词第 12 步：报告生成与文件导出模块

请帮我实现多格式数据导出和报告生成功能。

### 页面需构建的内容

#### 导出中心页（/export/center）

**数据导出版块**：导出类型卡片（行情数据/量化结果/因子数据/回测结果/交易记录）；选择参数后导出 CSV/Excel

**图表导出版块**：图表类型选择（K线图/热力图/网络图/散点图/资产曲线图）；格式选择（PNG/JPG/SVG）

**报告导出版块**：报告类型卡片（回测报告/分析报告）；生成 PDF 或 Word 文档

**批量导出**：多文件打包为 ZIP 下载

### 后端接口

**导出（ExportController，路径 /api/export，需认证）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/export/data | POST | 数据导出，参数{type, codes[], startDate, endDate, format} |
| /api/export/chart | POST | 图表导出，参数{chartType, config, format} |
| /api/export/report | POST | 报告生成，参数{reportType, params, format} |
| /api/export/batch | POST | 批量打包导出，返回ZIP |

**验收标准**：
- CSV/Excel 导出内容完整、格式正确
- 图表导出分辨率清晰
- PDF/Word 报告排版正常

---

## 提示词第 13 步：系统监控与运维管理模块

请帮我实现系统监控和运维管理功能。

### 页面需构建的内容

#### 1. 系统监控面板（/system/monitor）
- 展示：CPU/内存/磁盘使用率仪表盘；接口调用量TOP10和平均响应时间趋势图；数据库连接池状态

#### 2. 操作日志页（/system/log）
- 若已有，增加按模块筛选、导出Excel功能

#### 3. 运维工具栏（/system/maintenance）
- 缓存管理：一键清理缓存（需确认弹窗）
- 数据维护：重建模拟数据、数据库备份
- 健康检查：系统健康状态面板

### 后端接口

**监控（MonitorController，路径 /api/monitor，需管理员角色）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/monitor/server | GET | 查询服务器资源使用情况 |
| /api/monitor/api-stats | GET | 查询接口调用统计 |
| /api/monitor/middleware | GET | 查询中间件状态 |

**运维（MaintenanceController，路径 /api/maintenance，需管理员角色）：**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/maintenance/cache/clear | POST | 清理所有缓存 |
| /api/maintenance/cache/stats | GET | 查询缓存统计 |
| /api/maintenance/data/rebuild | POST | 重建模拟数据 |
| /api/maintenance/data/backup | POST | 数据库备份 |
| /api/maintenance/health-check | GET | 执行健康检查 |

**验收标准**：
- CPU和内存数据能正确获取
- 操作日志可按条件检索
- 健康检查能反映真实系统状态

---

## 提示词第 14 步：系统集成、联调测试与最终优化

请帮我进行系统整体的集成联调、功能测试和最终优化。

### 需要完成的工作

1. **菜单导航集成**：
   - 确保所有页面在左侧菜单中能正确访问，菜单层级结构为：
     - 数据看板：股票分析、实时行情、市场监控
     - 量化分析：相关性分析、联动性分析、因子分析
     - 数据管理：基础信息、行情数据、财务数据、市场概览（采集+治理各一组Tab）
     - 策略回测：策略模板、回测配置、回测记录
     - **虚拟交易**：交易首页、买入股票、卖出股票、交易记录、盈亏分析、账户管理
     - 智能预警：预警规则、预警记录、消息通知
     - 批量任务：批量任务、任务管理
     - 数据统计：数据状态、采集日志、自选股中心、导出中心
     - 系统管理：用户管理、角色管理、菜单管理、权限管理、数据权限、系统配置、定时任务、操作日志、系统监控、运维工具
   - 每个菜单项配置对应的权限标识

2. **全局功能完善**：
   - 顶部导航栏：消息通知图标（带未读角标）、用户头像下拉菜单（个人中心/退出登录）
   - 侧边栏：折叠/展开、当前路由高亮
   - 面包屑导航：显示当前页面路径

3. **交互体验优化**：
   - 所有表格支持排序、筛选
   - 所有请求添加 Loading 动画
   - 所有删除操作添加二次确认弹窗
   - 统一错误提示和成功提示样式
   - 数据加载失败时显示"重试"按钮
   - 长时间任务显示进度条

4. **性能优化**：
   - 图表按需加载
   - 大表格使用分页
   - 静态资源使用浏览器缓存

5. **测试验证**：
   - 登录后遍历所有菜单项，确保每个页面正常渲染
   - 执行完整的虚拟交易流程（创建账户 → 充值 → 买入 → 卖出 → 查看盈亏）
   - 执行完整的量化分析流程（选择股票 → 相关性分析 → 查看热力图）
   - 执行完整的策略回测流程（选择策略 → 配置参数 → 执行回测 → 查看结果）
   - 验证权限控制：使用 viewer01 登录确认看不到管理菜单

6. **H2 提示**：在系统首页添加提示信息："当前系统使用 H2 内存数据库，服务重启后所有数据将重置。"

### 验收标准
- 所有菜单项能正确导航到对应页面
- 完整的虚拟交易流程走通无报错
- 完整的量化分析流程走通无报错
- 权限控制生效，不同角色看到不同菜单
- 页面无明显卡顿，交互体验流畅

---

## 附录：完整数据库表汇总（37张）

| 序号 | 表名 | 所属模块 |
|------|------|----------|
| 1 | sys_user | 用户权限 |
| 2 | sys_role | 用户权限 |
| 3 | sys_permission | 用户权限 |
| 4 | sys_menu | 用户权限 |
| 5 | sys_user_role | 用户权限-关联 |
| 6 | sys_role_menu | 用户权限-关联 |
| 7 | sys_role_permission | 用户权限-关联 |
| 8 | sys_user_data_permission | 用户权限 |
| 9 | sys_config | 系统管理 |
| 10 | sys_scheduled_task | 任务调度 |
| 11 | sys_operation_log | 系统管理 |
| 12 | sys_collection_log | 数据采集 |
| 13 | stock_info | 数据管理 |
| 14 | index_data | 数据管理 |
| 15 | sector_info | 数据管理 |
| 16 | collection_basic_info | 数据采集 |
| 17 | collection_market_data | 数据采集 |
| 18 | collection_financial_data | 数据采集 |
| 19 | collection_market_overview | 数据采集 |
| 20 | governance_basic_info | 数据治理 |
| 21 | governance_market_data | 数据治理 |
| 22 | governance_financial_data | 数据治理 |
| 23 | governance_market_overview | 数据治理 |
| 24 | quant_calculation_result | 量化分析 |
| 25 | strategy_info | 策略回测 |
| 26 | backtest_record | 策略回测 |
| 27 | backtest_trade | 策略回测 |
| 28 | stock_group | 自选股 |
| 29 | favorite_stock | 自选股 |
| 30 | virtual_account | **虚拟交易（新增）** |
| 31 | virtual_position | **虚拟交易（新增）** |
| 32 | virtual_order | **虚拟交易（新增）** |
| 33 | virtual_transaction | **虚拟交易（新增）** |
| 34 | virtual_asset_history | **虚拟交易（新增）** |
| 35 | warning_rule | 智能预警 |
| 36 | warning_record | 智能预警 |
| 37 | notification_message | 消息通知 |

## 附录：执行顺序

| 步骤 | 模块 | 核心重点 |
|------|------|----------|
| 第1步 | 项目环境初始化 | 框架搭建 |
| 第2步 | 数据库建模与初始数据 | 37张表 + 模拟数据 |
| 第3步 | 用户认证与权限管理 | RBAC权限体系 |
| 第4步 | 数据采集与治理 | 8个页面 + 日志统计 |
| 第5步 | 股票行情分析可视化 | K线图 + 多股票对比 |
| 第6步 | 量化关系计算引擎 | 相关性/联动性/因子 |
| 第7步 | 量化策略回测 | 6个策略模板 |
| **第8步** | **虚拟交易模块（H2）** | **5张新表 + 6个页面** |
| 第9步 | 实时行情WebSocket | 实时推送 |
| 第10步 | 智能预警与通知 | 预警规则+站内信 |
| 第11步 | 批量任务调度 | 异步任务+定时调度 |
| 第12步 | 报告与文件导出 | 多格式导出 |
| 第13步 | 系统监控与运维 | 监控面板+运维工具 |
| 第14步 | 系统集成与联调 | 整体联调测试 |

共计 **14 个步骤**，包含约 170 个后端接口、40+ 个前端页面、37 张数据库表。