# 招聘网站后端（web-back）

基于 **Java 17 + Spring Boot 3.2.2 + MyBatis Plus 3.5.5 + MySQL 8** 的招聘网站后端。

- 第01步：Spring Boot 工程搭建
- 第02步：数据绑定与接口实现（内存 MockDataStore）
- 第03步：接入 MySQL，用 MyBatis Plus 持久化，删除 MockDataStore
- **第06步：JWT 认证（登录发 Token，拦截器校验）** ← 当前进度

- 端口：`8080`
- 统一前缀：`/api`（`server.servlet.context-path`）
- 统一响应：`{ "code": 200, "message": "操作成功", "data": ... }`，业务失败时 `code = 500`
- 鉴权：受保护接口需 `Authorization: Bearer <token>`，未登录统一返回 HTTP 401
- 接口文档：<http://localhost:8080/api/swagger-ui/index.html>（右上角 Authorize 贴 Token）

## 项目结构

```
web-back/
├── pom.xml                                   # mybatis-plus、mysql-connector-j、springdoc、jjwt
├── postman/                                  # 课件 Postman 集合副本
├── tools/
│   ├── verify-api.ps1                        # 一键回归脚本（37 项断言，打真实接口）
│   └── verify-result.json                    # 最近一次执行的明细
├── docs/第06步-截图/                          # 第06步 3 张提交截图
└── src/main/
    ├── java/com/recruitment/
    │   ├── RecruitmentSystemApplication.java
    │   ├── common/Result.java                # 统一响应
    │   ├── config/
    │   │   ├── JacksonConfig.java            # LocalDateTime → yyyy-MM-dd HH:mm:ss
    │   │   ├── MyBatisPlusConfig.java        # 分页拦截器
    │   │   ├── WebMvcConfig.java             # 【第06步】注册 JWT 拦截器 + 白名单
    │   │   └── SwaggerConfig.java            # 【第06步】Bearer Token 安全方案
    │   ├── context/UserContext.java          # 【第06步】ThreadLocal 保存当前 userId
    │   ├── controller/                       # Index / Hello / Job / User / Resume / Favorite / Application
    │   ├── interceptor/JwtInterceptor.java   # 【第06步】验 Token、写 401、放行
    │   ├── mapper/                           # 5 个 Mapper，全部 extends BaseMapper<实体>
    │   ├── model/                            # 实体，@TableName + @TableId(AUTO)
    │   ├── service/ + service/impl/          # Service 注入 Mapper，不再有静态仓库
    │   ├── util/JwtUtil.java                 # 【第06步】生成 / 验证 / 解析 Token
    │   └── vo/LoginVO.java                   # 【第06步】token + user
    └── resources/
        ├── application.yml                   # 数据源 + MyBatis Plus + jwt 配置
        └── db/init.sql                       # 建库、建表、演示数据
```

> 第02步的 `data/MockDataStore.java` 已在第03步删除，数据全部落在 MySQL。

## 启动方式

### 1. 初始化数据库（只需一次，或需要重置数据时）

```powershell
# 命令行执行
mysql -u root -p < src/main/resources/db/init.sql
```

或在 IDEA 的 Database 工具窗口里打开 `src/main/resources/db/init.sql`，选好数据源整段执行。

脚本会：建库 `recruitment_db` → 重建 `user` / `job` / `resume` / `favorite` / `application` 五张表 → 写入 8 个用户（`demo` 为 id=1）和 3 条职位。

### 2. 确认账号密码

`src/main/resources/application.yml` 里的 `spring.datasource` 默认是本机开发账号：

```yaml
username: root
password: 123456
```

换成你自己的 MySQL 账号密码后即可启动。

### 3. 启动

```bash
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/recruitment-system-1.0.0.jar --server.port=8080
```

启动后访问 <http://localhost:8080/api/> 应返回接口清单 JSON；控制台会打印 MyBatis 执行的 SQL（`log-impl: StdOutImpl`）。

## 数据访问链路

```
Controller（绑参）
  → Service（业务规则、参数校验）
    → Mapper（BaseMapper + LambdaQueryWrapper，框架生成 SQL）
      → MySQL（recruitment_db）
```

- 单表按主键：`selectById` / `insert` / `updateById` / `deleteById`
- 按其他字段：`LambdaQueryWrapper` + `selectOne` / `selectList`
- 实体与表：`@TableName("job")`、`@TableId(type = IdType.AUTO)`，字段靠 `map-underscore-to-camel-case` 自动映射

## 接口一览（前缀 `http://localhost:8080/api`）

### 自检

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/` | 接口清单（根路径不再 404） |
| GET | `/hello` | 欢迎语 |
| GET | `/hello/info` | 系统信息 |
| GET | `/hello/health` | 健康检查 |
| GET | `/hello/now` | 当前时间 |

### 5.1 职位

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/jobs/list?city=&keyword=` | 列表；城市、关键词可单独或组合筛选（`eq` + `and(like or like)`） |
| GET | `/jobs/{id}` | 详情；`view_count` 每次 +1 并写回数据库 |

### 5.2 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/users/register` | 注册；用户名重复返回「用户名已存在」；响应不含 `passWord`（白名单） |
| POST | `/users/login` | 登录；返回 `{ token, user }`，`user` 不含密码（白名单） |
| GET | `/users/info` | **当前登录用户**，userId 由 Token 解析，不接收前端传参（需 Token） |
| GET | `/users/{id}` | 用户信息（脱敏，需 Token） |

### 5.3 简历（CRUD）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/resumes` | 新建；校验用户存在，补 `createTime` / `updateTime` |
| GET | `/resumes/{id}` | 详情 |
| PUT | `/resumes/{id}` | 部分更新：只覆盖非 null 字段 |
| DELETE | `/resumes/{id}` | 删除 |
| GET | `/resumes/list?userId=` | 某用户的简历列表 |

### 5.4 收藏

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/favorites/add` | Body `{userId, jobId}`；重复返回「已收藏」 |
| DELETE | `/favorites/remove?userId=&jobId=` | 取消收藏（物理删除） |
| GET | `/favorites/list?userId=` | 返回 **Job 列表** |

### 5.5 投递

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/applications/apply` | 投递；已投递返回「已投递」，取消后可「重新投递成功」 |
| PUT | `/applications/cancel?userId=&jobId=` | 取消：**只改状态**，记录保留 |
| GET | `/applications/list?userId=` | 含 jobId / jobTitle / companyName / status / applyTime / cancelTime |

## 演示数据

| 类型 | 内容 |
|---|---|
| 用户 | `demo / 123456`（id=1）… 共 8 个，便于测分页 |
| 职位 | 1 阿里巴巴 / Java开发工程师 / 杭州 / 15K-30K；2 腾讯科技 / 前端开发工程师 / 深圳；3 字节跳动 / Go开发工程师 / 北京 |

## 接口回归验证

```powershell
# 服务已启动的前提下
powershell -ExecutionPolicy Bypass -File tools\verify-api.ps1
```

37 项断言（26 个课件请求 + 根路径自检 + 4 项提交接口 + 6 项第06步 JWT），结果写入 `tools/verify-result.json`。
脚本会先登录拿 Token，再自动给受保护接口补 `Authorization: Bearer <token>`，白名单接口不带。
脚本会改数据，重跑前建议先重跑 `init.sql` 还原。

最近一次结果：**37/37 通过**，见 [第06步-接口验证报告.md](第06步-接口验证报告.md)。

## JWT 认证

请求链路：

```
登录 → 后端签发 Token → 前端保存 → 后续请求带 Authorization: Bearer <token>
   → JwtInterceptor.preHandle 验签 → UserContext.setCurrentUserId(userId)
   → Controller / Service 读 UserContext → afterCompletion 里 clear()
```

- 密钥与有效期在 `application.yml` 的 `jwt.secret` / `jwt.expiration`（默认 7 天）
- Token 里只放 `userId`（`subject`）、签发时间、过期时间；**不放密码等敏感信息**
- 未登录 / Token 无效返回 **HTTP 401**：`{"code":401,"message":"请先登录"}`
- 白名单见 `WebMvcConfig`：`/users/login`、`/users/register`、`/jobs/**`、`/hello/**`、Swagger 路径等

第06步的 3 张提交截图和复现步骤见 [第06步-提交清单.md](第06步-提交清单.md)。

## 常见问题

**1. 访问 `http://localhost:8080/api` 是 Whitelabel 404？**
根路径已补 `IndexController`，`/api` 与 `/api/` 返回接口清单。业务接口都在 `/api` 前缀下，形如 `http://localhost:8080/api/jobs/list`。

**2. 启动报 `Access denied for user 'root'@'localhost'`？**
`application.yml` 的 `username` / `password` 与 MySQL 实际账号不一致。

**3. 启动报 `Unknown database 'recruitment_db'`？**
`init.sql` 还没执行，或库名被改过。

**4. 启动报 `Communications link failure`？**
MySQL 服务没起（`Get-Service MySQL80`），或端口不是 3306。

**5. 端口不是 8080 / 提示被占用？**
本机若注入过 `SERVER__PORT` 环境变量会覆盖配置，用参数强制：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

**6. 想清空测试数据？**
重跑 `src/main/resources/db/init.sql`（会 DROP 重建），或只删某张表的数据：
```sql
USE recruitment_db;
DELETE FROM application; DELETE FROM favorite; DELETE FROM resume;
DELETE FROM user WHERE id > 8;
UPDATE job SET apply_count = 0, view_count = 0;
```

**7. 取消后重新投递，`cancelTime` 为什么能清空？**
`updateById` 默认忽略 null 字段。重新投递处用 `LambdaUpdateWrapper` 显式 `set(cancelTime, null)`，否则旧的取消时间会留在库里。

**8. 访问 `/users/info` 返回 401「请先登录」？**
这是第06步的正常行为。先 `POST /users/login` 拿 `data.token`，再在请求头加 `Authorization: Bearer <token>`；Swagger 里点右上角 **Authorize** 粘贴 token 即可。

**9. 粘贴 Token 后仍提示「Token无效或已过期」？**
有两种常见原因：一是自己手写了 `Bearer ` 前缀，Swagger 的 Value 框里只贴裸 Token；二是重启后改过 `jwt.secret`，旧 Token 的签名对不上了，重新登录即可。

**10. Swagger 页面 404 / 打不开？**
确认 `pom.xml` 里有 `springdoc-openapi-starter-webmvc-ui`，且 `WebMvcConfig` 白名单包含 `/swagger-ui/**`、`/v3/api-docs/**`；访问地址是 `/api/swagger-ui/index.html`（注意 context-path）。
