package org.fyh.zk.lock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.fyh.zk.lock.dao")
@SpringBootApplication
public class ZkDestributeLockApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZkDestributeLockApplication.class, args);
	}
}
