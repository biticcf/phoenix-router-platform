/**
 * 
 */
package com.beyond.phoenix.router.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.beyond.phoenix.router.feign.PmOrgQueryClient;
import com.beyond.phoenix.router.feign.TokenCheckClient;
import com.beyond.phoenix.router.filter.SwaggerHeaderFilter;
import com.beyond.phoenix.router.filter.TokenAuthFilter;
import com.beyond.phoenix.router.model.UriModel;

/**
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午7:10:27
 *
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "voyager.token.check")
@Configuration
public class VoyagerConfig {
	
	@Value("${pm.org.check.enabled:false}")
	private Boolean checkPmOrg;
	@Value("${pm.org.check.expire-time:3600}")
	private int expireTime;
	
	private List<UriModel> whiteUri = new ArrayList<>();
	private List<UriModel> blackUri = new ArrayList<>();
	private int expireToken;
	
	@Bean
	public SwaggerHeaderFilter swaggerHeaderFilter() {
		return new SwaggerHeaderFilter();
	}
	
	@Bean
	public TokenAuthFilter tokenAuthFilter(
			TokenCheckClient tokenCheckClient, PmOrgQueryClient pmOrgQueryClient) {
		return new TokenAuthFilter(tokenCheckClient, whiteUri, blackUri, 
				pmOrgQueryClient, checkPmOrg, expireTime, expireToken);
	}
	
	public List<UriModel> getWhiteUri() {
		return whiteUri;
	}

	public void setWhiteUri(List<UriModel> whiteUri) {
		this.whiteUri = whiteUri;
	}

	public List<UriModel> getBlackUri() {
		return blackUri;
	}

	public void setBlackUri(List<UriModel> blackUri) {
		this.blackUri = blackUri;
	}

	public int getExpireToken() {
		return expireToken;
	}

	public void setExpireToken(int expireToken) {
		this.expireToken = expireToken;
	}
}
