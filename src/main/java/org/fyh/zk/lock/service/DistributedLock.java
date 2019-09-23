package org.fyh.zk.lock.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description:zk锁
 * @author:fangyunhe
 * @time:2018年5月14日 下午7:32:30
 * @version 1.0
 */
@Slf4j
public class DistributedLock implements Lock, Watcher {
	private ZooKeeper zk = null;

	/**
	 * 根节点
	 */
	private String rootLock = "/locks";

	/**
	 * 竞争的资源
	 */
	private String lockName;

	/**
	 * 等待的前一个锁
	 */
	private String waitLock;

	/**
	 * 当前锁
	 */
	private String currentLock;

	/**
	 * 计数器
	 */
	private CountDownLatch countDownLatch;

	/**
	 * 判断是否连接计数器
	 */
	private CountDownLatch connectedCountDownLatch = new CountDownLatch(1);

	/**
	 * zk节点分割符
	 */
	private String division = "/";

	private int sessionTimeout = 30000;

	public DistributedLock(String config, String lockName) throws Exception {
		this.lockName = lockName;
		// 连接zookeeper
		zk = new ZooKeeper(config, sessionTimeout, this);
		waitUntilConnected(zk, connectedCountDownLatch);
		Stat stat = zk.exists(rootLock, false);
		if (stat == null) {
			// 如果根节点不存在，则创建根节点
			zk.create(rootLock, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
	}

	/**
	 * zk 链接时事件监听，避免未链接情况下创建节点报错
	 * 
	 * @param zooKeeper
	 * @param connectedLatch
	 * @throws InterruptedException
	 * @author:fangyunhe
	 * @time:2018年5月14日 下午7:36:27
	 */
	private void waitUntilConnected(ZooKeeper zooKeeper, CountDownLatch connectedLatch) throws InterruptedException {
		if (States.CONNECTING == zooKeeper.getState()) {
			connectedLatch.await();
		}
	}

	/**
	 * 节点监视器
	 */
	@Override
	public void process(WatchedEvent event) {
		if (this.countDownLatch != null) {
			this.countDownLatch.countDown();
		}
		//是否已经连接
		//添加一个zk已连接的事件监听，避免zktcp未连接导致节点创建不成功报错：KeeperErrorCode = NodeExists for /locks
		if (event.getState() == KeeperState.SyncConnected) {
			connectedCountDownLatch.countDown();
			log.info("zk已连接。。。");
		}
	}

	@Override
	public void lock() {
		try {
			if (this.tryLock()) {
				log.info(Thread.currentThread().getName() + " " + lockName + "获得了锁");
				return;
			} else {
				// 等待锁
				waitForLock(waitLock, sessionTimeout);
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		} catch (KeeperException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean tryLock() {
		try {
			String splitStr = "_lock_";
			if (lockName.contains(splitStr)) {
				throw new Exception("锁名有误");
			}
			// 创建临时有序节点
			currentLock = zk.create(rootLock + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			if (currentLock != null) {
				log.info(currentLock + " 已经创建");
			} else {
				log.info("报错。。。。。。。。");
			}
			// 取所有子节点
			List<String> subNodes = zk.getChildren(rootLock, false);
			// 取出所有lockName的锁
			List<String> lockObjects = new ArrayList<String>();
			for (String node : subNodes) {
				String itemNode = node.split(splitStr)[0];
				if (itemNode.equals(lockName)) {
					lockObjects.add(node);
				}
			}
			Collections.sort(lockObjects);
			log.info(Thread.currentThread().getName() + " 的锁是 " + currentLock);
			// 若当前节点为最小节点，则获取锁成功
			if (currentLock.equals(rootLock + division + lockObjects.get(0))) {
				return true;
			}

			// 若不是最小节点，则找到自己的前一个节点
			String lastPathString = currentLock.substring(currentLock.lastIndexOf("/") + 1);
			waitLock = lockObjects.get(Collections.binarySearch(lockObjects, lastPathString) - 1);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		} catch (KeeperException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) {
		try {
			if (this.tryLock()) {
				return true;
			}
			return waitForLock(waitLock, timeout);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 等待锁
	 * 
	 * @param prev
	 * @param waitTime
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author:fangyunhe
	 * @time:2018年5月14日 下午7:41:29
	 */
	private boolean waitForLock(String prev, long waitTime) throws KeeperException, InterruptedException {
		Stat stat = zk.exists(rootLock + "/" + prev, true);
		if (stat != null) {
			log.info(Thread.currentThread().getName() + "等待锁 " + rootLock + "/" + prev);
			this.countDownLatch = new CountDownLatch(1);
			// 计数等待，若等到前一个节点消失，则precess中进行countDown，停止等待，获取锁
			this.countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
			this.countDownLatch = null;
			log.info(Thread.currentThread().getName() + " 等到了锁");
		}
		return true;
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	/**
	 * 删除临时节点（客户端断开连接也会删除临时节点）
	 * @author:fangyunhe
	 * @time:2018年5月14日 下午7:41:29
	 */
	@Override
	public void unlock() {
		try {
			zk.delete(currentLock, -1);
			currentLock = null;
			zk.close();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		} catch (KeeperException e) {
			log.error(e.getMessage(), e);
		}

	}

}
