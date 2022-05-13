package rpc.in.action.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import rpc.in.action.core.Node;
import rpc.in.action.core.RpcCallContext;
import rpc.in.action.core.serviceregistry.RpcServicesRegistry;
import rpc.in.action.core.handler.RpcHandler;
import rpc.in.action.core.handler.RpcProtocolDecoder;
import rpc.in.action.core.handler.RpcProtocolEncoder;

import java.util.List;

/**
 * 服务提供
 * - netty-server
 * - 注册服务名和节点的关联信息(redis实现)
 * - 扫描所有rpc接口存储到注册表
 * - 收到消息反射调用方法返回结果
 *
 * 可优化点：
 * 1. 服务提供者和注册中心之间的保持心跳，超过某阈值进行服务剔除
 * 2. 异步调用
 * 3. 参数/结果解析和类型转换
 * 4. 重用事件循环(Client)
 */
@Slf4j
public class ProviderServer {

    /**
     * 服务注册表
     */
    private final RpcServicesRegistry servicesRegistry;
    /**
     * rpc调用注册表 Map<Rpc唯一值,Method>
     */
    private final RpcCallContext rpcCallContext;

    private final Node node;

    private final String servicesName;

    public ProviderServer(List<String> packages, String servicesName, String hostname, int port) {
        this.rpcCallContext = new RpcCallContext(packages);
        this.servicesName = servicesName;
        this.node = new Node(hostname, port);
        this.servicesRegistry = new RpcServicesRegistry();
        /*1. 注册服务名*/
        servicesRegistry.register(servicesName, this.node);
        /*2. 启动*/
        start();
    }

    public void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(node.getHostname(), node.getPort())
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast(new RpcProtocolDecoder())
                                    .addLast(new RpcProtocolEncoder())
                                    .addLast(new RpcHandler(rpcCallContext));
                        }
                    });
            ChannelFuture cf = serverBootstrap.bind().syncUninterruptibly();
            cf.channel().closeFuture().syncUninterruptibly();
        } catch (Error e) {
            log.error("Netty Rpc Server Start Error..");
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            servicesRegistry.offline(servicesName, node);
        }
    }
}
