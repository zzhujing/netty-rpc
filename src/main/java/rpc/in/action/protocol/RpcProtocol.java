package rpc.in.action.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.Serializable;

/**
 * 自定义rpc协议
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = 1L;

    private ListenableFuture<Object> future;
    /**
     * 服务名
     */
    private String servicesName;
    /**
     * 请求id
     */
    private String requestId;

    /**
     * 调用方法的唯一标识
     */
    private String callMethod;

    /**
     *  参数列表json
     */
    private String parameterJson;

    /**
     * 响应结果
     */
    private String resp;
}
