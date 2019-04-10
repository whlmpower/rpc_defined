package com.whl.rpc_registry;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceDiscovery extends  ServiceZk{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();



    public ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;
        watch();
    }

    private void watch(){
        ZooKeeper zk = connect();
        if (zk != null){
            watchNode(zk);
        }

    }

    /**
     * 监听 变化  更新List
     * @param zk
     * 参数加final，意味着无法在方法中修改参数，使其指向另一个对象
     */
    private void watchNode(final ZooKeeper zk){
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                                watchNode(zk);
                            }
                        }
                    });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/"
                                + node, false, null);
                dataList.add(new String(bytes));
            }

            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }


    /**
     * 发现新节点
     * @return
     */
    public String discover(){
        String data = null;
        int size = dataList.size();

        if (size > 0){
            if (size == 1){
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            }else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;

    }


}
