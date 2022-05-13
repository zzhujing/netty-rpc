package rpc.in.action.core.handler;

import com.alibaba.fastjson.JSONArray;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rpc.in.action.core.RpcCallContext;
import rpc.in.action.protocol.RpcProtocol;

import java.lang.reflect.Method;

@AllArgsConstructor
@Slf4j
public class RpcHandler extends SimpleChannelInboundHandler<RpcProtocol> {

    private final RpcCallContext rpcCallContext;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        log.info("Receive Rpc Request Info :{}",msg);
        if (msg == null) {
            return;
        }
        String callMethod = msg.getCallMethod();
        String parameterJson = msg.getParameterJson();
        Method method = rpcCallContext.getMethodByRpcFlag(callMethod);
        Object resp = method.invoke(rpcCallContext.getInvokeObjByMethod(method), JSONArray.parseArray(parameterJson, Object.class).toArray());
        msg.setResp(resp + "");
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
    }
}
