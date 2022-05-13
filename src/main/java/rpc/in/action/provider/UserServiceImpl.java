package rpc.in.action.provider;

import com.google.common.util.concurrent.Uninterruptibles;
import rpc.in.action.core.annotation.Rpc;

import java.util.concurrent.TimeUnit;

public class UserServiceImpl  implements UserService{

    @Rpc
    @Override
    public String getById(String json) {
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
        return "hello";
    }
}
