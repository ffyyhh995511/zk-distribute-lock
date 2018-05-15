package org.fyh.zk.lock.dao;

import org.fyh.zk.lock.entity.Good;

public interface GoodMapper {
    int deleteByPrimaryKey(String id);

    int insert(Good record);

    int insertSelective(Good record);

    Good selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Good record);

    int updateByPrimaryKey(Good record);
}