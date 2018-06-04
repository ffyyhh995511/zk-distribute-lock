package org.fyh.zk.lock.controller;

import javax.annotation.Resource;

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
	
}
