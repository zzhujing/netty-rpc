package rpc.in.action.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务节点
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node implements Serializable {

    private static final long serialVersionUID = 1L;
    private String hostname;
    private int port;
}
