package rpc.in.action.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import rpc.in.action.core.annotation.Rpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * rpc调用注册表
 */
public class RpcCallContext {

    /**
     * RPC提供方类接口方法名 - 方法
     */
    private ConcurrentMap<String, Method> callRegistry;
    /**
     * 提供方 方法 - 具体实现类
     */
    private ConcurrentMap<Method,Object> callObj;
    private List<String> scanPackageName;
    private final ResourcePatternResolver resourcePatternResolver;
    private final MetadataReaderFactory metadataReaderFactory;

    public RpcCallContext() {
        this(null);
    }

    public RpcCallContext(List<String> scanPackageName) {
        if (CollUtil.isNotEmpty(scanPackageName))
            this.scanPackageName = scanPackageName;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        this.callRegistry = new ConcurrentHashMap<>();
        this.callObj = new ConcurrentHashMap<>();
        this.metadataReaderFactory = new CachingMetadataReaderFactory();
        //需要扫描所有的Rpc注解，将所有方法在启动的时候填充到里面
        scanRpcMetadata();
    }

    /**
     * 扫描指定包下的rpc元信息
     * 标注@Rpc的方法会被扫描
     */
    private void scanRpcMetadata() {
        Objects.requireNonNullElse(this.scanPackageName, Collections.singletonList(this.getClass().getPackageName())).forEach(pac -> {
            try {
                Resource[] resources = resourcePatternResolver.getResources("classpath*:" + pac.replace('.', '/') + "/*.class");
                Stream.of(resources)
                        .forEach(res -> {
                            try {
                                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(res);
                                String klassName = metadataReader.getClassMetadata().getClassName();
                                Class<?> klass = Class.forName(klassName);
                                Stream.of(klass.getDeclaredMethods()).filter(m -> m.getDeclaredAnnotation(Rpc.class) != null)
                                        .forEach(m -> {
                                            Rpc rpc = m.getDeclaredAnnotation(Rpc.class);
                                            callRegistry.put(StrUtil.isBlank(rpc.value()) ? m.getName() : rpc.value(), m);
                                            try {
                                                callObj.put(m, klass.getDeclaredConstructor().newInstance());
                                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                                e.printStackTrace();
                                            }
                                        });
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException("Resolve Resource " + res.getFilename() + " Error..");
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据rpc唯一值获取实现方法
     * @param rpcFlag
     * @return
     */
    public Method getMethodByRpcFlag(String rpcFlag) {
        return callRegistry.getOrDefault(rpcFlag, null);
    }

    /**
     * 根据指定方法获取对象
     * @param method
     * @return
     */
    public Object getInvokeObjByMethod(Method method) {
        return callObj.get(method);
    }

    public static void main(String[] args) {
        RpcCallContext callRegistry = new RpcCallContext(Collections.singletonList("rpc.in.action.provider"));
        callRegistry.scanRpcMetadata();
        callRegistry.callRegistry.forEach((name, method) -> System.out.println("name = " + name + " , method =" + method));
    }
}
