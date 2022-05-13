package rpc.in.action.consumer.loadbalancer;

import rpc.in.action.core.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final String currentServiceName;

    public static final Map<String, AtomicInteger> SERVICE_CACHE = new HashMap<>();

    public RoundRobinLoadBalancer(String currentServiceName) {
        this.currentServiceName = currentServiceName;
    }

    @Override
    public Node choose(List<Node> nodes) {
        AtomicInteger incr = SERVICE_CACHE.computeIfAbsent(currentServiceName, currentServiceName -> new AtomicInteger());
        return nodes.get(incr.getAndIncrement() % nodes.size());
    }
}
