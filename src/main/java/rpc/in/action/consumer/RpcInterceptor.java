package rpc.in.action.consumer;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import rpc.in.action.core.annotation.Rpc;
import rpc.in.action.protocol.RpcProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 服务消费者(调用方),对特定注解方法进行代理拦截
 * 1. 拦截方法调用(动态代理) @RpcClient下面的所有@
 * 2. netty-client建立连接进行请求并等待响应
 */
public class RpcInterceptor implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Rpc rpc = method.getDeclaredAnnotation(Rpc.class);
        String callMethod = rpc == null ? method.getName() : rpc.value();
        RpcProtocol request = RpcProtocol.builder()
                .servicesName(method.getDeclaringClass().getDeclaredAnnotation(rpc.in.action.core.annotation.RpcClient.class).servicesName())
                .callMethod(callMethod)
                .parameterJson(JSON.toJSONString(args))
                .requestId(RandomUtil.randomString(10))
                .build();
        //构建连接进行调用
        RpcClient client = new RpcClient(request.getServicesName());
        CompletableFuture<Object> cf = client.execAsync(request);
        System.out.println("success");
        return cf.join();
    }
}
