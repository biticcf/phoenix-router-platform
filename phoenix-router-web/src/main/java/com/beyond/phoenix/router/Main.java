/**
 * 
 */
package com.beyond.phoenix.router;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午8:11:23
 * SpringBoot 主方法
 * @SpringBootApplication相当于
 *     @Configuration+@EnableAutoConfiguration+@ComponentScan
 */
@SpringBootApplication(scanBasePackages = {"com.beyond.voyager.routeb"})
//@EnableEurekaClient
//@EnableCircuitBreaker
@EnableFeignClients(basePackages = {"com.beyond.voyager.routeb.feign"})
public class Main {
	// jar启动入口
	public static void main(String[] args) throws Exception {
		configureApplication(new SpringApplicationBuilder()).run(args);
	}
	
	private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
        return builder.sources(Main.class).bannerMode(Banner.Mode.CONSOLE).logStartupInfo(true).registerShutdownHook(true).web(WebApplicationType.REACTIVE);
    }
}
