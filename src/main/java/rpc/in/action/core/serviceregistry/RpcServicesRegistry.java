package rpc.in.action.core.serviceregistry;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import rpc.in.action.core.Node;

import java.util.Collections;
import java.util.List;

/**
 * rpc服务注册表(基于redis)
 */
public class RpcServicesRegistry {

    private final HashOperations<String,String,String> redisClient;
    public static final String KEY = "services-registry";
    public static final String HOST = "localhost";
    public static final int PORT = 6379;

    public RpcServicesRegistry() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(HOST, PORT);
        config.setDatabase(2);
//        config.setPassword("MXOd6ugMd65q1PsOBXPea1XIOclR");
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();
        this.redisClient = new StringRedisTemplate(connectionFactory).opsForHash();
    }

    /**
     * 注册服务节点
     * @param servicesName
     * @param node
     */
    public void register(String servicesName, Node node) {
        Object nodesJson = redisClient.get(KEY, servicesName);
        List<Node> nodes = JSON.parseArray(String.valueOf(nodesJson), Node.class);
        if(CollUtil.isEmpty(nodes))
            nodes = Lists.newArrayList();
        if (!nodes.contains(node)) {
            nodes.add(node);
            redisClient.put(KEY, servicesName, JSON.toJSONString(nodes));
        }
    }

    /**
     * 获取节点名称
     * @param servicesName
     * @return
     */
    public List<Node> getNodesByName(String servicesName) {
        Object nodes = redisClient.get(KEY, servicesName);
        if(nodes == null) {
            return Collections.emptyList();
        }
        return JSONArray.parseArray(String.valueOf(nodes), Node.class);
    }

    /**
     * 服务下线
     */
    public void offline(String servicesName,Node node) {
        Object nodes = redisClient.get(KEY, servicesName);
        if(nodes != null) {
            List<Node> nodeList = JSONArray.parseArray(String.valueOf(nodes), Node.class);
            nodeList.remove(node);
            redisClient.put(KEY,servicesName,JSONArray.toJSONString(nodeList));
        }
    }
}
