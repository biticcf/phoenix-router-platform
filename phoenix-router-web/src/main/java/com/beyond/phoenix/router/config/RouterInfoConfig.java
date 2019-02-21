/**
 * 
 */
package com.beyond.phoenix.router.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.beyond.phoenix.router.model.RouterInfoModel;

/**
 * @Author: DanielCao
 * @Date:   2018年8月31日
 * @Time:   下午3:45:58
 *
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConfigurationProperties(prefix = "spring.cloud.gateway")
@Configuration
public class RouterInfoConfig {
	private List<RouterInfoModel> routes = new ArrayList<>();

	public List<RouterInfoModel> getRoutes() {
		return routes;
	}

	public void setRoutes(List<RouterInfoModel> routes) {
		this.routes = routes;
	}
}
