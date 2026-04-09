package com.finflow.application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest(properties = { 
    "spring.cloud.config.enabled=false",
    "jwt.secret=9a6211c696860a920274c43346736a2a07c0800b4119d5c4b1219b160b72c918" 
})
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
class ApplicationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
