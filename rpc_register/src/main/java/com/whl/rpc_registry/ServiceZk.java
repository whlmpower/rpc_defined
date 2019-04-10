package com.whl.rpc_registry;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public abstract class ServiceZk {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    protected String registryAddress;


    /**
     * 创建连接，监听
     * @return
     */
    protected ZooKeeper connect(){
        ZooKeeper zk = null;

        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

}
