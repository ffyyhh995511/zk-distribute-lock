package org.fyh.zk.lock.service;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.fyh.zk.lock.dao.GoodMapper;
import org.fyh.zk.lock.dao.PurchaseHistoryMapper;
import org.fyh.zk.lock.entity.Good;
import org.fyh.zk.lock.entity.PurchaseHistory;
import org.springframework.stereotype.Service;

@Service
public class GoodsService {

	@Resource
	private GoodMapper goodMapper;

	@Resource
	private PurchaseHistoryMapper purchaseHistoryMapper;

	public int distributeShopping() throws Exception {
		DistributedLock lock = new DistributedLock("127.0.0.1:2181", "test1");
		lock.lock();
		processPurchaseHistory();
		lock.unlock();
		return 1;
	}

	public void processPurchaseHistory() {
		Good good = goodMapper.selectByPrimaryKey("1");
		Integer amount = good.getAmount();
		if (amount > 0) {
			PurchaseHistory purchaseHistory = new PurchaseHistory();
			purchaseHistory.setId(UUID.randomUUID().toString().replace("-", ""));
			purchaseHistory.setGoodName(good.getName());
			purchaseHistory.setUid(UUID.randomUUID().toString().replace("-", ""));
			purchaseHistory.setCreateTime(new Date());
			purchaseHistoryMapper.insert(purchaseHistory);
			good.setAmount(amount - 1);
			goodMapper.updateByPrimaryKeySelective(good);
		}
	}

}
