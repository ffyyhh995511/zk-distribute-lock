package org.fyh.zk.lock.dao;

import org.fyh.zk.lock.entity.PurchaseHistory;

public interface PurchaseHistoryMapper {
    int deleteByPrimaryKey(String id);

    int insert(PurchaseHistory record);

    int insertSelective(PurchaseHistory record);

    PurchaseHistory selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PurchaseHistory record);

    int updateByPrimaryKey(PurchaseHistory record);
}