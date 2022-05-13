package rpc.in.action.consumer;

import rpc.in.action.core.annotation.RpcClient;

@RpcClient(servicesName = "user-services")
public interface UserService {

    String getById(String json);
}
