# 招聘网站后端（web-back）

基于 **Java 17 + Spring Boot 3.2.2 + Maven** 的招聘网站后端。目前完成到 **第02步：数据绑定与接口实现（下）**，包含职位、用户、简历、收藏、投递五个模块，数据暂存在内存（`MockDataStore`），不接数据库。

- 端口：`8080`
- 统一前缀：`/api`（`server.servlet.context-path`）
- 统一响应：`{ "code": 200, "message": "操作成功", "data": ... }`，业务失败时 `code = 500`

## 项目结构

```
web-back/
├── pom.xml
├── tools/
│   ├── verify-api.ps1                    # 一键回归脚本（31 项断言）
│   └── verify-result.json                # 最近一次执行的明细
└── src/main/
    ├── java/com/recruitment/
    │   ├── RecruitmentSystemApplication.java
    │   ├── common/Result.java                 # 统一响应：code / message / data
    │   ├── config/JacksonConfig.java          # LocalDateTime → yyyy-MM-dd HH:mm:ss
    │   ├── controller/
    │   │   ├── IndexController.java           # GET /api/ 返回接口清单（避免 Whitelabel 404）
    │   │   ├── HelloController.java           # /hello 系列自检接口
    │   │   ├── JobController.java             # /jobs
    │   │   ├── UserController.java            # /users
    │   │   ├── ResumeController.java          # /resumes
    │   │   ├── FavoriteController.java        # /favorites
    │   │   └── ApplicationController.java     # /applications
    │   ├── data/MockDataStore.java            # 内存仓库 + 预置数据
    │   ├── model/                             # User / Job / Resume / Favorite / Application
    │   └── service/ + service/impl/           # 五个模块的接口与实现
    └── resources/application.yml
```

## 启动方式

```bash
# 方式一：命令行
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/recruitment-system-1.0.0.jar --server.port=8080
```

启动后访问 <http://localhost:8080/api/> 应看到接口清单 JSON。

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

| 方法 | 路径 | 绑参 | 说明 |
|---|---|---|---|
| GET | `/jobs/list?city=&keyword=` | `@RequestParam` | 列表；城市、关键词可单独或组合筛选 |
| GET | `/jobs/{id}` | `@PathVariable` | 详情；`viewCount` 每次 +1，不存在返回「职位不存在」 |

### 5.2 用户（`@RequestBody`）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/users/register` | 注册；用户名重复返回「用户名已存在」，响应不含 `passWord` |
| POST | `/users/login` | 登录；账号或密码错误统一返回「用户名或密码错误」 |
| GET | `/users/{id}` | 用户信息（同样脱敏） |

### 5.3 简历（一套完整 CRUD）

| 方法 | 路径 | 绑参 | 说明 |
|---|---|---|---|
| POST | `/resumes` | `@RequestBody` | 新建，自动补 `createTime` |
| GET | `/resumes/{id}` | `@PathVariable` | 详情 |
| PUT | `/resumes/{id}` | 路径 id + Body | 部分更新：只覆盖非 null 字段 |
| DELETE | `/resumes/{id}` | `@PathVariable` | 删除 |
| GET | `/resumes/list?userId=` | `@RequestParam` | 某用户的简历列表 |

### 5.4 收藏

| 方法 | 路径 | 绑参 | 说明 |
|---|---|---|---|
| POST | `/favorites/add` | Body `{userId, jobId}` | 收藏；重复返回「已收藏」 |
| DELETE | `/favorites/remove?userId=&jobId=` | `@RequestParam` | 取消收藏（物理删除） |
| GET | `/favorites/list?userId=` | `@RequestParam` | 返回 **Job 列表**，不是 Favorite |

### 5.5 投递

| 方法 | 路径 | 绑参 | 说明 |
|---|---|---|---|
| POST | `/applications/apply` | Body `{userId, jobId}` | 投递；已投递返回「已投递」，取消后可「重新投递成功」 |
| PUT | `/applications/cancel?userId=&jobId=` | `@RequestParam` | 取消：**只改状态**，记录保留 |
| GET | `/applications/list?userId=` | `@RequestParam` | 含 jobId / jobTitle / companyName / status / applyTime / cancelTime |

## 预置数据（重启后还原）

| 类型 | 内容 |
|---|---|
| 用户 | `demo / 123456`（id=1，昵称「示例用户」） |
| 职位 | 1 阿里巴巴 / Java开发工程师 / 杭州；2 腾讯科技 / 前端开发工程师 / 深圳；3 字节跳动 / Go开发工程师 / 北京 |

## 接口回归验证

```powershell
# 服务已启动的前提下
powershell -ExecutionPolicy Bypass -File tools\verify-api.ps1
```

脚本按课件顺序执行 26 个请求 + 根路径自检 + 3 个提交接口，共 31 项断言，结果写入 `tools/verify-result.json`。
**注意**：脚本会改内存数据，重跑前请重启应用；最后一次执行结果见 [第02步-接口验证报告.md](第02步-接口验证报告.md)。

## 常见问题

**1. 访问 `http://localhost:8080/api` 看到 Whitelabel Error Page / `No static resource .`？**
说明请求落到了根路径而应用没有 `/` 映射。现在已补 `IndexController`，`GET /api` 和 `GET /api/` 会返回接口清单。
但业务接口都在 `/api` 前缀下，正确的地址形如 `http://localhost:8080/api/jobs/list`；单纯访问 `http://localhost:8080/`（不带 `/api`）不属于本应用，仍是 404。

**2. 端口不是 8080 / 启动报端口占用？**
本机若注入过 `SERVER__PORT` 环境变量，会覆盖 `application.yml` 里的端口。用参数强制：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

**3. 数据为什么每次重启都变回去？**
`MockDataStore` 是内存存储，进程结束即清空，只剩 demo 用户和 3 条职位。截图前建议重启一次，再按课件顺序操作。

**4. JSON 时间格式**
`spring.jackson.date-format` 只对 `java.util.Date` 生效。`LocalDateTime` 由 `config/JacksonConfig.java` 统一序列化为 `yyyy-MM-dd HH:mm:ss`。
