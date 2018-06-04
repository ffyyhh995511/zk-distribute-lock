package org.fyh.zk.lock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * @Description:zk分布式锁启动类
 * @author:fangyunhe
 * @time:2018年6月4日 下午4:31:49
 * @version 1.0
 */
@MapperScan("org.fyh.zk.lock.dao")
@SpringBootApplication
public class ZkDestributeLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZkDestributeLockApplication.class, args);
	}
}
