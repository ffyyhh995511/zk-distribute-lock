package org.fyh.zk.lock.controller;

import javax.annotation.Resource;

import org.fyh.zk.lock.service.DistributedLock;
import org.fyh.zk.lock.service.GoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 
 * @Description:测试
 * @author:fangyunhe
 * @time:2018年6月4日 上午10:42:12
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class TestController {
	
	DistributedLock lock;
	
	@Resource
	private GoodsService goodsService;
	
	/**
	 * 用户抢购
	 * 
	 * @return
	 * @throws Exception
	 * @author:fangyunhe
	 * @time:2018年5月15日 下午5:13:38
	 */
	@RequestMapping("/purchase")
	public Object purchase() throws Exception {
		int result = goodsService.distributeShopping();
		return "成功:"+result;
	}
	
	/**
	 * 测试获取锁，zk的生产临时节点
	 * @return
	 * @throws Exception
	 * @author:fangyunhe
	 * @time:2019年9月24日 上午9:36:33
	 */
	@RequestMapping("/t1")
	public Object t() throws Exception {
		lock = new DistributedLock("127.0.0.1:2181", "childNode");
		lock.lock();
		return "成功";
	}
	
	/**
	 * 关闭临时节点
	 * @return
	 * @throws Exception
	 * @author:fangyunhe
	 * @time:2019年9月24日 上午9:37:03
	 */
	@RequestMapping("/t2")
	public Object t2() throws Exception {
		lock.unlock();
		return "成功";
	}
	
}
