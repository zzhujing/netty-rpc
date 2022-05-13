package rpc.in.action.consumer.loadbalancer;

import rpc.in.action.core.Node;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡策略
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public Node choose(List<Node> nodes) {
        return nodes.get(ThreadLocalRandom.current().nextInt(nodes.size() - 1));
    }
}
