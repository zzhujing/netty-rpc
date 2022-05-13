package rpc.in.action.demo;

import rpc.in.action.provider.ProviderServer;

import java.util.Collections;

public class ServerStart {

    public static void main(String[] args) {

        new ProviderServer(
                Collections.singletonList("rpc.in.action.provider"),
                "user-services",
                "localhost", 9090
        );
    }
}
