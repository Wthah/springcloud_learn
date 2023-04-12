package com.zane.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ZookeeperClientTest {

    private static final String ZK_ADDRESS="127.0.0.1:2181";

    private static final int SESSION_TIMEOUT = 5000;

    private static ZooKeeper zooKeeper;

    private static final String ZK_NODE="/zk-node/test";

    private static final String ZK_NODE_ASYNC = "/zk-node-async";

    @Before
    public void init() throws IOException, InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(ZK_ADDRESS,SESSION_TIMEOUT,watchedEvent -> {
           if(watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected&&watchedEvent.getType()==Watcher.Event.EventType.None) {
               countDownLatch.countDown();
               log.info("连接成功");
           }
        });
        log.info("连接中");
        countDownLatch.await();
    }

    //同步创建节点
    @Test
    public void createNode() throws KeeperException, InterruptedException {
        //参数说明
        //第一个参数是path路劲
        //第二个参数是data
        //第三个参数是acl权限
        //第四个参数是节点类型
        String path = zooKeeper.create(ZK_NODE, "data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.debug("create path:{}",path);
    }

    //异步创建节点
    @Test
    public void createNodeAsync() throws InterruptedException {
        /**
         * 参数说明
         * 第一个是path路径
         * 第二个是data
         * 第三个是acl权限
         * 第四个是节点类型
         * 第五个是回调接口 rc: 响应码， path：节点路径， ctx：上下文， name: 节点名称
         */
        zooKeeper.create(ZK_NODE_ASYNC, "data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, (rc,path,ctx,name)->{
            Thread thread = Thread.currentThread();
            log.debug("currentThread:{}, rc:{}, paht:{}, ctx:{}, name:{}",thread.getName(),rc,path,ctx,name);
        },"centext");
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void updateNode() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData(ZK_NODE, false, stat);
        log.debug("修改前：{}", new String(data));
        //通过version实现乐观锁 期间有其他线程也修改了数据则更新不成功
        zooKeeper.setData(ZK_NODE, "changeed".getBytes(), stat.getVersion());
        byte[] dataAfter = zooKeeper.getData(ZK_NODE, false, stat);
        log.debug("修改后：{}", new String(dataAfter));
    }
}
