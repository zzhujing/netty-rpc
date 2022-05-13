package rpc.in.action.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import rpc.in.action.consumer.loadbalancer.LoadBalancer;
import rpc.in.action.consumer.loadbalancer.RoundRobinLoadBalancer;
import rpc.in.action.core.NoProviderServicesException;
import rpc.in.action.core.Node;
import rpc.in.action.core.handler.RpcProtocolDecoder;
import rpc.in.action.core.handler.RpcProtocolEncoder;
import rpc.in.action.core.serviceregistry.RpcServicesRegistry;
import rpc.in.action.protocol.RpcProtocol;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * rpc服务调用客户端
 */
public class RpcClient {

    /**
     * rpc服务注册表(redis实现)
     */
    private static final RpcServicesRegistry rpcServicesRegistry = new RpcServicesRegistry();


    private final EventLoopGroup group;
    /**
     * 负载均衡策略
     */
    private LoadBalancer loadBalancer;
    private ChannelFuture cf;

    public RpcClient(String servicesName) {
        this.group = new NioEventLoopGroup(1);
        this.cf = cf;
        this.loadBalancer = new RoundRobinLoadBalancer(servicesName);
    }

    /**
     * 同步调用
     *
     * @param protocol
     * @return
     */
    public Object execSync(RpcProtocol protocol) {
        return req(protocol).join();
    }


    /**
     * 异步调用
     *
     * @param rpcProtocol
     * @return
     */
    public CompletableFuture<Object> execAsync(RpcProtocol rpcProtocol) {
        return req(rpcProtocol);
    }

    private CompletableFuture<Object> req(RpcProtocol protocol) {
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        completableFuture.whenComplete((res, t) -> close());
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .localAddress("127.0.0.1", RandomUtil.randomInt(10000, Short.MAX_VALUE >> 1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast(new RpcProtocolDecoder())
                                .addLast(new RpcProtocolEncoder())
                                .addLast(new RpcClientHandler(protocol, completableFuture));
                    }
                });
        //根据服务名获取node
        List<Node> nodes = rpcServicesRegistry.getNodesByName(protocol.getServicesName());
        if (CollUtil.isEmpty(nodes)) {
            throw new NoProviderServicesException();
        }
        //负载均衡
        Node node = loadBalancer.choose(nodes);
        this.cf = bootstrap.connect(node.getHostname(), node.getPort()).syncUninterruptibly();
        //发送请求
        return completableFuture;
    }

    private void close() {
        group.shutdownGracefully();
    }

}
