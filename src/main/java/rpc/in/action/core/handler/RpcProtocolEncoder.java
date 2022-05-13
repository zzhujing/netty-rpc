package rpc.in.action.core.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import rpc.in.action.protocol.RpcProtocol;

import java.nio.charset.StandardCharsets;

public class RpcProtocolEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) {
        String json = JSONObject.toJSONString(msg);
        byte[] fullMessage = json.getBytes(StandardCharsets.UTF_8);
        out.writeInt(fullMessage.length);
        out.writeBytes(fullMessage);
    }
}
