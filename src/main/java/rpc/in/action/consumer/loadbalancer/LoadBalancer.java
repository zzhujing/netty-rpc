package rpc.in.action.consumer.loadbalancer;


import rpc.in.action.core.Node;

import java.util.List;

/**
 * 负载均衡器
 */
public interface LoadBalancer {

    Node choose(List<Node> nodes);
}
