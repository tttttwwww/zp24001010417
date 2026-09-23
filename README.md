# 招聘网站后端（web-back）

第01步：Spring Boot 项目搭建。基于 Java 17 + Spring Boot 3.2.2 + Maven 的招聘网站后端工程。

## 项目结构

```
web-back/
├── pom.xml                              # Maven 工程（Spring Boot 3.2.2 + Java 17）
└── src/main/
    ├── java/com/recruitment/
    │   ├── RecruitmentSystemApplication.java   # 主启动类
    │   ├── common/Result.java                  # 统一响应类：code / message / data
    │   └── controller/HelloController.java     # 测试接口 /api/hello 系列
    └── resources/application.yml               # 端口 8080、前缀 /api、时间格式
```

## 运行方式

**方式一：IDEA 直接运行**
打开 `RecruitmentSystemApplication`，点绿色三角运行 `main`，控制台出现「招聘网站后端启动成功！」。

**方式二：命令行运行**
```bash
mvn spring-boot:run
```

## 接口一览（统一前缀 /api，端口 8080）

| 地址 | 作用 |
|---|---|
| `GET /api/hello` | 欢迎语 |
| `GET /api/hello/info` | 系统信息（项目名 / 版本 / 运行状态） |
| `GET /api/hello/health` | 健康检查（课上练习） |
| `GET /api/hello/now` | 当前时间（课上练习） |

所有接口统一返回 `Result` 三字段：
```json
{ "code": 200, "message": "操作成功", "data": ... }
```
