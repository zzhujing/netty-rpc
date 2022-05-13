package rpc.in.action.demo;

import rpc.in.action.consumer.RpcInterceptor;
import rpc.in.action.consumer.UserService;

import java.lang.reflect.Proxy;

public class ClientStart {

    public static void main(String[] args) {
        UserService proxy = (UserService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{UserService.class}, new RpcInterceptor());
        System.out.println(proxy.getById("123"));
    }
}
