# 猫咪品种参考系统（cat_card）
## 项目描述
为养宠物的用户提供猫咪品种参考，支持多维度模糊查询（品种名、饲养环境、性格、预算等），结合 Redis 缓存优化查询性能，保证接口高可用。

## 环境依赖（必看！）
### 基础环境
- JDK：1.8（适配 Spring Boot 2.7.6，其他版本可能出现兼容异常）
- MySQL：5.7/8.0（需提前创建 cat_card 数据库，并导入猫咪基础数据）
- Redis：3.0+（支持单机/哨兵模式，哨兵模式需按指定步骤启动）
- Maven：3.6+（用于项目打包/编译）

### 核心依赖说明
- Spring Boot 2.7.6：项目基础框架
- MyBatis-Plus 3.4.0：简化 MySQL CRUD 操作
- Redis：缓存优化（防穿透/击穿/雪崩）+ 分布式 Session
- Fastjson2 2.0.32：JSON 序列化（缓存数据转换）

## 启动前配置
### 数据库 & Redis 配置
修改 `src/main/resources/application.yml` 中的以下配置（替换为自身环境信息）：
~~~yml
spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/cat_card?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root  # 替换为你的 MySQL 用户名
    password: 123456  # 替换为你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  # Redis 哨兵模式配置
  redis:
    sentinel:
      master: mymaster  # 哨兵监控的主节点名称
      nodes: 127.0.0.1:26379  # 多个哨兵节点用逗号分隔（例：127.0.0.1:26379,127.0.0.1:26380）
    password:  # 如果Redis有密码，填写这里（主从/哨兵密码需一致）
    database: 0  # 使用第0个数据库
    timeout: 5000  # 连接超时（毫秒）
    lettuce:
      pool:
        max-active: 10  # 连接池最大连接数
        max-idle: 5     # 连接池最大空闲连接数
        min-idle: 2     # 连接池最小空闲连接数
        max-wait: 3000  # 获取连接的最大等待时间（毫秒）
      shutdown-timeout: 100  # 关闭连接的超时时间（毫秒）
~~~

## 启动方式

###运行 CatCardApplication.java  
- 打开idea，运行该主类
### 运行 cat_card-0.0.1-SNAPSHOT.jar 包
- 切换到项目文件夹下运行该命令 java -jar cat_card-0.0.1-SNAPSHOT.jar

# 相关命令(Windows)
- 执行打包：       mvn clean package -DskipTests
## 启动Redis+哨兵
- 主节点：                              redis-server --port 6379
- 从节点：                              redis-server --port 6380 --slaveof 127.0.0.1 6379
- 哨兵(需要先创建sentinel26379.conf)：    redis-server.exe sentinel26379.conf --sentinel
- 
- 启动nacos(进入bin目录运行命令)           startup.cmd -m standalone

- 执行redis-cli连接主节点（6379）                .\redis-cli.exe -p 6379      
- 验证是否连接成功（输入以下命令，返回PONG则正常）     ping
- 连接哨兵（26379）                             .\redis-cli.exe -p 26379

- 清空缓存（测试用）                              FLUSHDB
- 清空Redis缓存（测试缓存命中用）                  .\redis-cli.exe -p 6379 FLUSHDB
- 停止Redis主节点（测试哨兵切换用）                 .\redis-cli.exe -p 6379 SHUTDOWN
- 查看主节点信息                                 sentinel master mymaster

- 查看所有缓存key（测试缓存是否生成）               KEYS cat_card:*
- 查看缓存内容                                  GET 缓存key
- 查看缓存过期时间（秒）                          TTL 缓存key
- 查看主节点状态                                 sentinel master mymaster
- 查看从节点列表                                 sentinel slaves mymaster
- 查看哨兵自身信息                               sentinel info
##git基础命令
- 添加                   git add .
- 日志信息                git commit -m "修改了xx功能"
- 推送                   git push



# sentinel26379.conf配置文件参考
~~~
port 26379
sentinel monitor mymaster 127.0.0.1 6379 1
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 10000
~~