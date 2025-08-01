<h1 align="center">
  <br>
  Shortcut
  <h4 align="center">
基于 Spring Boot 的高性能短地址生成服务，使用 Twitter 雪花算法生成唯一短地址，支持密码保护和二维码生成。
  </h4>
  <h5 align="center">
<a href="#环境要求">环境要求</a>&nbsp;&nbsp;
<a href="#快速开始">快速开始</a>&nbsp;&nbsp;
<a href="#功能特性">功能特性</a>&nbsp;&nbsp;
<a href="#配置说明">配置说明</a>&nbsp;&nbsp;
<a href="#架构设计">架构设计</a>&nbsp;&nbsp;
<a href="#性能测试">性能测试</a>&nbsp;&nbsp;
<a href="#致谢">致谢</a>&nbsp;&nbsp;
<a href="#许可证">许可证</a>
</h5>
  <br>
</h1>

![GitHub](https://img.shields.io/github/license/Alkaids/shortcut)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/alkaids/shortcut)
![Java Version](https://img.shields.io/badge/Java-8%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.4-green)

## 环境要求

* **Java**: 8+
* **Maven**: 3.0+
* **Redis**: 4.0+

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/RipperTs/shortcut.git
cd shortcut
```

### 2. 配置 Redis
修改 `src/main/resources/application.yml` 中的 Redis 配置：
```yaml
spring:
  redis:
    host: your-redis-host
    port: 6379
    password: your-redis-password
```

### 3. 构建运行
```bash
# 跳过测试构建
mvn -Dmaven.test.skip=true clean package

# 运行应用
java -jar target/shortcut-0.0.1-SNAPSHOT.jar

# 或直接 Maven 运行
mvn spring-boot:run
```

### 4. 访问服务
- **Web 界面**: http://127.0.0.1:9527/
- **API 接口**: http://127.0.0.1:9527/convert

### 5. 主页面
![main.png](docs/main.png)

## 功能特性

### ✅ 已实现功能
- 🔗 **短地址生成**: 基于雪花算法的高性能短地址生成
- 🔒 **密码保护**: 支持为URL转换设置密码验证
- 📱 **二维码生成**: 自动生成对应的二维码图片
- 🔍 **布隆过滤器**: 高效判断URL是否已存在
- 🌐 **自定义域名**: 支持配置自定义短地址域名
- 🎨 **Web界面**: 现代化的前端界面，支持加载指示器
- 🔄 **反向查询**: 支持通过短地址查询原始URL
- 🗑️ **删除功能**: 支持删除已创建的短地址
- 🌍 **跨域支持**: 内置CORS配置，支持跨域请求
- 📄 **错误处理**: 自定义404错误页面和全局异常处理
- ⚡ **高性能**: QPS 可达 4000+
- 🛡️ **异常处理**: 全局异常拦截和处理
- ✔️ **URL校验**: 完善的URL格式验证
- 🗂️ **缓存配置**: 支持自定义Redis缓存前缀

### 🚧 计划功能
- [ ] 令牌桶限流
- [ ] URL访问统计
- [ ] 批量URL转换
- [ ] 过期时间设置

## 配置说明

### 基础配置
```yaml
# 服务端口
server:
  port: 9527

# 自定义域名配置
common:
  domain: http://your-domain.com
```

### 安全配置
```yaml
# 密码保护配置
security:
  passwords: 
    - admin123      # 支持多个密码
    - convert2024
```

### 缓存配置
```yaml
# Redis缓存配置
cache:
  prefix: shortcut_  # 缓存key前缀
```

## 架构设计

### 核心原理

参考知乎上[这篇文章](https://www.zhihu.com/question/29270034/answer/46446911)的短地址生成方法，主要包含两个步骤：

1. **实现唯一ID发号器** - 使用Twitter雪花算法生成不重复的Long型ID
2. **进制转换** - 将Long型ID转换为62进制字符串作为短地址

### 雪花算法结构

```
0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 
```

- **1位符号位**: 固定为0（正数）
- **41位时间戳**: 毫秒级时间戳，可使用69年
- **10位机器标识**: 支持1024个节点部署
- **12位序列号**: 同一毫秒内可生成4096个ID

### 核心组件

#### 控制器层
- **MainController**: URL转换、二维码生成、密码验证
- **RedirectController**: 短地址重定向
- **IndexController**: 前端页面渲染

#### 服务层
- **UrlConvertService**: URL转换核心服务接口
- **UrlConvertServiceImpl**: 服务实现，集成布隆过滤器和Redis存储

#### 工具类
- **SnowFlake**: Twitter雪花算法实现
- **NumericConvertUtils**: 62进制转换工具
- **QRcodeUtils**: 基于ZXing的二维码生成工具
- **Validator**: URL格式校验工具
- **BloomFilter**: 布隆过滤器实现

#### 配置类
- **SecurityProperties**: 密码验证配置
- **CacheProperties**: Redis缓存前缀配置
- **RedisConfiguration**: Redis连接配置

### 处理流程

1. **URL转换流程**:
   ```
   用户提交base64编码URL → 密码验证 → base64解码 → URL格式校验 
   → 布隆过滤器判重 → 雪花算法生成ID → 62进制转换 
   → Redis存储映射 → 返回短地址
   ```

2. **短地址访问流程**:
   ```
   访问短地址 → 解析短码 → Redis查询编码URL → base64解码 → 302重定向
   ```

3. **二维码生成**:
   ```
   接收URL参数 → 使用ZXing生成二维码 → 返回图片流
   ```

### 数据存储

- **Redis存储结构**: `{prefix}{shortCode} → base64EncodedUrl`
- **布隆过滤器**: 基于base64编码URL快速判断是否已存在，减少Redis查询
- **编码机制**: 存储时使用base64编码，访问时自动解码

## API 接口

### 1. URL转换
```http
POST /convert
Content-Type: application/json

{
  "url": "aHR0cHM6Ly9leGFtcGxlLmNvbQ==",
  "password": "admin123"
}
```

**注意**: `url` 参数必须是经过 **base64编码** 的URL字符串。

**编码示例**:
```javascript
// 原始URL: https://example.com
// Base64编码后: aHR0cHM6Ly9leGFtcGxlLmNvbQ==
const originalUrl = "https://example.com";
const encodedUrl = btoa(encodeURIComponent(originalUrl));
```

### 2. 短地址反查
```http
POST /revert
Content-Type: application/json

{
  "shortUrl": "7TDp0rS917i"
}
```

**返回**: 解码后的原始URL

### 3. 删除短地址
```http
POST /delete
Content-Type: application/json

{
  "shortUrl": "7TDp0rS917i"
}
```

**返回**: `{"code":200,"message":"SUCCESS","data":true}`

### 4. 二维码生成
```http
GET /qrcode?url=https://example.com
```

**注意**: 二维码接口可以直接使用原始URL，无需base64编码。

### 5. 检查密码验证状态
```http
GET /password-enabled
```

**返回**: `{"code":200,"message":"SUCCESS","data":true}`


### 测试配置
```java
Options options = new OptionsBuilder()
    .include(BenchmarkTest.class.getName() + ".*")
    .warmupIterations(1)        // 预热轮数
    .warmupTime(TimeValue.seconds(1))
    .measurementIterations(5)   // 测试轮数
    .measurementTime(TimeValue.seconds(5))
    .forks(1)                   // 进程数
    .threads(16)                // 线程数
    .build();
```

### 测试结果
```
Benchmark                      Mode  Cnt    Score    Error  Units
BenchmarkTest.httprequest     thrpt    5  1948.349 ± 2028.032  ops/s
BenchmarkTest.serviceRequest  thrpt    5  3945.100 ± 1185.980  ops/s
```

- **HTTP请求**: QPS约2000，通过OkHttp发送POST请求测试
- **服务直调**: QPS约4000，直接调用服务方法测试

### 优化建议
- 进制转换部分存在不必要的类型转换，可进一步优化
- 可考虑使用连接池优化Redis连接
- 布隆过滤器参数可根据实际数据量调优

## 部署说明

### Docker 部署

#### 方式一：Docker Compose（推荐）

**前提条件**：确保已构建好 jar 文件
```bash
mvn -Dmaven.test.skip=true clean package
```

**使用 docker-compose 一键部署**：
```bash
# 启动服务（包含应用和Redis）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

**docker-compose.yml 配置**：
```yaml
version: '3.8'

services:
  redis:
    image: registry.cn-hangzhou.aliyuncs.com/ripper/redis:7-alpine
    container_name: shortcut-redis
    restart: unless-stopped
    command: redis-server --requirepass 123456
    volumes:
      - ./redis-data:/data
    networks:
      - shortcut-network

  shortcut:
    image: registry.cn-hangzhou.aliyuncs.com/ripper/shortcut:latest
    container_name: shortcut-app
    restart: unless-stopped
    ports:
      - "9527:9527"
    environment:
      # Redis配置
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=123456
      # 服务配置
      - SERVER_PORT=9527
      - COMMON_DOMAIN=http://localhost:9527
      # 缓存配置
      - CACHE_PREFIX=shortcut_
      # 安全配置
      - SECURITY_PASSWORDS[0]=admin123
      - SECURITY_PASSWORDS[1]=convert2024
    depends_on:
      - redis
    networks:
      - shortcut-network

networks:
  shortcut-network:
    driver: bridge
```

**环境变量配置说明**：
- 所有 `application.yml` 中的配置都可通过环境变量覆盖
- 转换规则：`spring.redis.host` → `SPRING_REDIS_HOST`
- 数组配置：`security.passwords[0]` → `SECURITY_PASSWORDS[0]`

#### 方式二：单独构建镜像

**Dockerfile**：
```dockerfile
FROM registry.cn-hangzhou.aliyuncs.com/ripper/openjdk:8-jdk-alpine
WORKDIR /app
COPY target/shortcut-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9527
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**构建和运行**：
```bash
# 构建镜像
docker build -t shortcut:latest .

# 运行容器（需要外部Redis）
docker run -p 9527:9527 \
  -e SPRING_REDIS_HOST=your-redis-host \
  -e SPRING_REDIS_PASSWORD=your-redis-password \
  shortcut:latest
```

### 生产环境配置
```yaml
# 生产环境建议配置
server:
  port: 80
  
spring:
  redis:
    host: redis-server
    port: 6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

common:
  domain: https://your-domain.com

security:
  passwords: 
    - ${CONVERT_PASSWORD}

cache:
  prefix: prod_shortcut_
```

## 致谢

感谢以下开源项目和资源：

* **[Spring Boot](https://spring.io/)** - 优秀的Java Web框架
* **[Redis](https://redis.io/)** - 高性能内存数据库
* **[Guava](https://github.com/google/guava)** - Google核心库
* **[ZXing](https://github.com/zxing/zxing)** - 二维码生成库
* **[JMH](http://openjdk.java.net/projects/code-tools/jmh/)** - Java性能基准测试工具
* **[Snowflake Algorithm](https://developer.twitter.com/en/docs/basics/twitter-ids)** - Twitter雪花算法
* **[知乎问答](https://www.zhihu.com/question/29270034/answer/46446911)** - 短地址生成方法参考

## 许可证

本项目基于 [MIT License](https://github.com/Alkaids/shortcut/blob/master/LICENSE) 开源。

---

## 更新日志

### v2.1.0 (最新)
- ✨ 新增短地址删除功能
- ✨ 新增CORS跨域支持
- ✨ 新增自定义404错误页面
- 🔧 重构API接口，统一使用JSON请求体
- 🎨 完善全局异常处理机制

### v2.0.0
- ✨ 新增密码保护功能
- ✨ 新增短地址反向查询接口
- ✨ 新增缓存前缀配置
- 🎨 优化Web界面，增加加载指示器
- 🔧 完善配置项和异常处理

### v1.0.0
- 🎉 基础短地址生成功能
- 🎉 二维码生成功能
- 🎉 布隆过滤器优化
- 🎉 性能基准测试