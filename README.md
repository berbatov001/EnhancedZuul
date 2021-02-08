# EnhancedZuul
EnhancedZuul是在Netflix Zuul 1.x的基础上封装的一个轻量级网关。
相比于Spring Cloud Zuul，它采用Nacos作为注册中心，EnhancedRibbon作为注册发现以及与下游微服务之间的通信工具。同时使用Sentinel作为限流组件。  
EnhancedZuul和EnhancedRibbon一起组成了比较完整的微服务框架，简单、快速、轻量级。

## 1.如何使用
### 1.1在你的Springboot工程中引入依赖
```xml
<dependency>
    <groupId>com.github.berbatov001</groupId>
    <artifactId>enhanced-zuul</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```  
### 1.2在application.properties文件中添加Nacos配置中心
```
nacos.discovery.serverAddr=(具体的Nacos集群地址)
```
### 1.3在application.properties文件中配置本网关代理的后端微服务（配置方法和Spring Cloud Zuul一致）
配置有两种方式：
``` 
# 方式1
# zuul.routes.#{Nacos上的服务名}.path = /#{路径1}/** 
# 例子如下：
zuul.routes.merchant.path=/merchant1/**
```
说明：merchant表示网关后面的一个微服务在Nacos配置中心的服务名，/merchant1/**表示网关代理这个微服务的所有接口，并在其接口路径前加/merchant1来标识。  
比如merchant服务有一个接口是/store/getItemList，那通过网关访问这个接口的完成路径就变成http://{网关IP}:{网关端口}/merchant1/store/getItemList。  
之前使用过Spring Cloud Zuul的朋友都会很熟悉这种配置方式。
``` 
# 方式2 
# zuul.routes.#{服务标识名}.service-id=#{Nacos上的服务名}
# zuul.routes.#{服务标识名}.path=/#{路径1}/** 
# 例子如下：
zuul.routes.merchant001.service-id=merchant
zuul.routes.merchant001.path=/merchant1/**
```
说明：这种方式配置不如方式1简洁，但由于Spring Cloud Zuul也支持这种方式，所以这里也保留了下来。#{服务标识名}是自己随便起，不一定和#{Nacos上的服务名}保持一致。
如果上面例子中，服务标识名是merchant001，但是该服务在Nacos中注册的名字是merchant。

### 1.4 补充说明（不是必须）
EnhancedZuul内部集成了Nacos的配置中心功能（看清，不是注册中心），能根据配置中心的变化提供多种Reload功能，非常方便，下面的章节会分别介绍。 所以推荐使用Nacos作为配置中心。   
首先需要在application.properties文件中配置Nacos配置中心的地址：
``` 
nacos.config.server-addr=(具体的Nacos集群地址)
```
之后在功能的启动类上加@NacosPropertySource注解即可  
``` java
@SpringBootApplication
@NacosPropertySource(dataId="这里填写Nacos上对应配置文件的名字", autoRefresh = true)
public class GatewayApplication{
    public static void main(String[] args){
       SpringAppliction.run(GatewayApplication.class, args);
    }
}
```
## 2.主要功能
除了最基础的反向代理外，相比于Spring Cloud Zuul，EnhancedZuul提供了一些新的功能。具体如下：
### 2.1 限流
EnhancedZuul在总入口上配置了Sentinel的限流功能。默认时关闭的，如需使用，只要在配置文件中打开开关即可。
``` 
# 限流开关，默认false（关闭）。
sentinel.enable=true
# 被限流请求的处理方式 0-直接失败（默认） 1-预热  2-排队等待
sentinel.behavior=0
# 排队等待的最大时间，单位毫秒。(只有在sentinel.behavior=2的时候有效)
sentinel.maxQueueingTimeMs=500
# 预热时间，单位秒。(只有在sentinel.behavior=1的时候有效)
sentinel.warmUpPeriodSec=10
# 网关总入口的QPS阈值，默认1000。
sentinel.uniqueEntranceQPSThreshold:1000
```
上述配置推荐配置在Nacos配置中心，EnhancedZuul内置了SentinelPropertiesNacosListener监听器，接收到Nacos相关配置变化之后，会自动刷新限流规则， 无需重启，实时生效。

### 2.2 动态添加删除后端代理服务
使用过Spring Cloud Zuul的朋友都知道，后端代理服务的配置都在application.properties文件中，每次新增服务或删除服务，都要修改配置并重启网关。  
EnhancedZuul重写了ZuulHandlerMapping类，根据ZuulPropertiesNacosListener监听器，监听Nacos相关配置变化自动刷新handlerMap。无论新增服务或删除服务，都无需重启，实时生效。
PS：这一功能必须使用Nacos配置中心。

### 2.3 按接口粒度设置请求超时时间
网关会代理后端的多个微服务，如果其中某个服务的接口响应很慢，遇到大量请求时，会占用网关的资源导致其它服务不可用。EnhancedZuul可以让你根据没个接口设置请求超时间，保护网关整理资源。只需要在网关的application.properties配置文件中如下配置即可：  
``` 
# connectTimeout指的是请求连接创建的最大时间，readTimeout值得时调用某一个接口的最大超时时间，如果接口响应较慢，到达指定的阈值，直接报错超时。具体用哪个根据自己的需求判断。
# readTimeout、connectTimeout、defaultConnectTimeout、defaultReadTimeout如果不配置，默认都是-1，表示有操作系统决定。
# 根据超时时间的不同将举要配置超值超时的接口分成多个组。
# 第一组针对/api1,/api2, 这两个接口，请i去超时时间设置成50秒，连接创建时间也设置成50秒。
rest-template.connections.group1.readTimeout=50000
rest-template.connections.group1.connectTimeout=50000
#多个接口中间用英文逗号隔开。
rest-template.connections.group1.url=/api1,/api2

# 第二组针对/api3,/api4, 这两个接口，请求去超时时间设置成70秒，连接创建时间不设置，默认为-1。
rest-template.connections.group2.readTimeout=70000
rest-template.connections.group2.url=/api3,/api4

# 其它接口，统一配置，超时时间和连接创建最大时间都是60秒。
rest-template.defaultConnectTimeout=60000
rest-template.defaultReadTimeout=60000
```
上述配置对接放在Nacos配置中心上，EnhancedZuul内置了RequestFactoryNacosListener监听器，无需重启，即时生效。

### 2.4 全链路灰度发布
通过向网关下游服务在Nacos上注册实例的MetaDate中添加版本号，来达到线上同时催在新老版本时，让普通浏览走老版本的服务路线，而指定的测试浏览走新版本的服务路线。EnhancedZuul的灰度功能默认时关闭的，需要手动开启。  
图一表示未开启灰度功能
图二表示打开灰度功能

#### 2.4.1 微服务启动时向Nacos注册自己的当前版本号
具体是在微服务应用的application.properties文件中做配置
``` 
nacos.dicovery.metadata.version=具体版本号
``` 
#### 2.4.2 定义一个灰度路线，和一个正常路线。
线路时一个Json对象，格式如下：
``` json
{
    "base_route":{"name":"线路1","routers":"{"serviceA":"1.0","serviceB":"1.0"}"},
    "gray_route":{"name":"线路2","routers":"{"serviceA":"2.0","serviceB":"2.0"}"}
}
```
其中base_route表示正常线路，gray_route表示灰度线路。routers表示当前线路所经历的所有微服务节点的版本。比如上面例子中，正常线路走的时serviceA的1.0版本实例，和serviceB的1.0版本实例。
#### 2.4.3 在网关的application.properties文件中开启灰度功能，并配置上面的路由线路和路由策略。
``` 
#开启灰度功能
gray.enable=true
#配置路由线路JSON
gray.grayScaleReleaseRoute={"base_route":{"name":"线路1","routers":"{"serviceA":"1.0","serviceB":"1.0"}"},"gray_route":{"name":"线路2","routers":"{"serviceA":"2.0","serviceB":"2.0"}"}}
#选择分流策略器，目前只支持IP白名单策略器
gray.strategy=IP
#配置当前分类策略器下的分流规则，当选择IP白名单策略器时，这里就配置Ip白名单。白名单内的IP走灰度路线，白名单以外的IP走正常路线。
gray.strategyRule=30.49.4.*,10.131.*.*,10.28.60.170,10.28.60.57
``` 
说明一下，IP白名单策略下会获取请求头中的X-Forwarded-For作为真实IP，所以请在外层的Nginx上开启X-Forwarded-For配置。