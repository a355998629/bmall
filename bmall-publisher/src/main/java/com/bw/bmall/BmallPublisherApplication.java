package com.bw.bmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bw.bmall.publisher.mapper")
public class BmallPublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmallPublisherApplication.class, args);
	}

}
