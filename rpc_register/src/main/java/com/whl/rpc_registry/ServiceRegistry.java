package com.whl.rpc_registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ServiceRegistry extends ServiceZk{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 创建连接，注册
     * @param data
     */
    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connect();
            if (zk != null){
                createNode(zk, data);
            }
        }

    }


    /**
     * 创建节点
     * @param zk
     * @param data
     */
    private void createNode(ZooKeeper zk, String data){
        byte[] bytes = data.getBytes();
        try {
            if (zk.exists(Constant.ZK_REGISTRY_PATH, null) == null){
                zk.create(Constant.ZK_REGISTRY_PATH, null,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            String path = zk.create(Constant.ZK_DATA_PATH, bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        }catch (Exception e) {
            LOGGER.error("", e);
        }
    }

}
