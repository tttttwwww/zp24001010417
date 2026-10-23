# 招聘网站后端（web-back）

基于 **Java 17 + Spring Boot 3.2.2 + MyBatis Plus 3.5.5 + MySQL 8** 的招聘网站后端。

- 第01步：Spring Boot 工程搭建
- 第02步：数据绑定与接口实现（内存 MockDataStore）
- **第03步：接入 MySQL，用 MyBatis Plus 持久化，删除 MockDataStore** ← 当前进度

- 端口：`8080`
- 统一前缀：`/api`（`server.servlet.context-path`）
- 统一响应：`{ "code": 200, "message": "操作成功", "data": ... }`，业务失败时 `code = 500`

## 项目结构

```
web-back/
├── pom.xml                                   # 含 mybatis-plus-spring-boot3-starter、mysql-connector-j
├── postman/                                  # 课件 Postman 集合副本
├── tools/
│   ├── verify-api.ps1                        # 一键回归脚本（31 项断言，打真实接口）
│   └── verify-result.json                    # 最近一次执行的明细
└── src/main/
    ├── java/com/recruitment/
    │   ├── RecruitmentSystemApplication.java
    │   ├── common/Result.java                # 统一响应
    │   ├── config/
    │   │   ├── JacksonConfig.java            # LocalDateTime → yyyy-MM-dd HH:mm:ss
    │   │   └── MyBatisPlusConfig.java        # 分页拦截器
    │   ├── controller/                       # Index / Hello / Job / User / Resume / Favorite / Application
    │   ├── mapper/                           # 5 个 Mapper，全部 extends BaseMapper<实体>
    │   ├── model/                            # 实体，@TableName + @TableId(AUTO)
    │   └── service/ + service/impl/          # Service 注入 Mapper，不再有静态仓库
    └── resources/
        ├── application.yml                   # 数据源 + MyBatis Plus 配置
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
| POST | `/users/register` | 注册；用户名重复返回「用户名已存在」；响应不含 `passWord` |
| POST | `/users/login` | 登录；账号或密码错误统一返回「用户名或密码错误」 |
| GET | `/users/{id}` | 用户信息（脱敏） |

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

31 项断言（26 个课件请求 + 根路径自检 + 4 项提交接口），结果写入 `tools/verify-result.json`。
脚本会改数据，重跑前建议先重跑 `init.sql` 还原。

最近一次结果：**31/31 通过**，见 [第03步-接口验证报告.md](第03步-接口验证报告.md)。

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
