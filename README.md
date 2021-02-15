# 欢迎使用EnhancedZuul
EnhancedZuul是在Netflix Zuul 1.x的基础上封装的一个轻量级网关。
相比于Spring Cloud Zuul，它采用Nacos作为注册中心，EnhancedRibbon作为注册发现以及与下游微服务之间的通信工具。同时使用Sentinel作为限流组件。  
EnhancedZuul和EnhancedRibbon一起组成了比较完整的微服务框架，简单、快速、轻量级。

EnhancedRibbon必须使用在Springboot 2.x版本的架构上。

详细设计和部署文档，请参考 [`WiKi`](https://github.com/berbatov001/EnhancedZuul/wiki)

# Features
EnhancedZuul具有以下特点：

1. 反向代理和负载均衡
2. 高性能
3. 继承Sentinel实现熔断限流
4. 动态添加删除后端代理服务
5. 按接口粒度设置请求超时时间
6. 使用Nacos作为注册中心和配置中心
7. 全链路灰度发布