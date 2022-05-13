# 自定义实现简易Netty-RPC

- 基于Redis#Hash的简易注册中心管理
- 注解接口声明
- 基于netty构建tcp访问

## 实现原理

### 提供方
- 构建Netty-Server，添加RpcHandler (ChannelHandler) ， 并注册节点信息到redis
- 通过包扫描@Rpc方法，填充Map<类#方法名,Method> 和Map<Method,实现类> 元信息

### 调用方
- 通过Jdk#Proxy代理调用接口，底层使用netty-client封装方法名称，请求参数等协议信息并从redis中获取节点信息，进行负载均衡策略连接调用
- 同步/异步等待返回结果

### 用例
见 rpc.in.action.demo 包


