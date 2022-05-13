package rpc.in.action.consumer;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import rpc.in.action.protocol.RpcProtocol;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientHandler extends ChannelDuplexHandler {

    private final RpcProtocol rpcProtocol;
    private final CompletableFuture<Object> cf;

    public RpcClientHandler(RpcProtocol rpcProtocol, CompletableFuture<Object> cf) {
        this.rpcProtocol = rpcProtocol;
        this.cf = cf;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(rpcProtocol);
        new Thread(() -> {
            synchronized (rpcProtocol) {
                log.info("Rpc调用写入成功，等待 " + ctx.channel().remoteAddress() + " 返回结果");
                try {
                    rpcProtocol.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Rpc调用写入成功，等待" + ctx.channel().remoteAddress() + " 返回结果 = " + rpcProtocol.getResp() );
                ctx.channel().close();
            }
        }).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcProtocol) {
            RpcProtocol protocol = (RpcProtocol) msg;
            synchronized (rpcProtocol){
                rpcProtocol.setResp(protocol.getResp());
                rpcProtocol.notifyAll();
                cf.complete(rpcProtocol.getResp());
            }
        }
    }
}
