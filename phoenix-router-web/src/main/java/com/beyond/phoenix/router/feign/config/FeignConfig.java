/**
 * 
 */
package com.beyond.phoenix.router.feign.config;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import feign.Feign;
import feign.Logger;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午7:35:14
 *
 */
@Import(FeignAutoConfiguration.class)
@Configuration
public class FeignConfig {
	private final Timer connectionManagerTimer = new Timer(
			"FeignApacheHttpClientConfiguration.connectionManagerTimer", true);

	@Autowired(required = false)
	private RegistryBuilder<?> registryBuilder;
	
	@Bean
    public Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
    
	@Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
	
//	@Bean
//    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
//        return new Jackson2ObjectMapperBuilderCustomizer() {
//            @Override
//            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
//            	jacksonObjectMapperBuilder.featuresToEnable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
//            	jacksonObjectMapperBuilder.defaultUseWrapper(true);
//            }
//        };
//    }
    
	@Bean
	@ConditionalOnMissingBean(HttpClientConnectionManager.class)
	public HttpClientConnectionManager connectionManager(
			ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
			FeignHttpClientProperties httpClientProperties) {
		final HttpClientConnectionManager connectionManager = connectionManagerFactory
				.newConnectionManager(httpClientProperties.isDisableSslValidation(), httpClientProperties.getMaxConnections(),
						httpClientProperties.getMaxConnectionsPerRoute(),
						httpClientProperties.getTimeToLive(),
						httpClientProperties.getTimeToLiveUnit(), registryBuilder);
		this.connectionManagerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				connectionManager.closeExpiredConnections();
			}
		}, 30000, httpClientProperties.getConnectionTimerRepeat());
		return connectionManager;
	}
	
	// httpclient参数配置,主要是重试策略DefaultHttpRequestRetryHandler(0, false)
	@Bean
	public HttpClient httpClient(ApacheHttpClientFactory httpClientFactory,
			HttpClientConnectionManager httpClientConnectionManager,
			FeignHttpClientProperties httpClientProperties) {
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(httpClientProperties.getConnectionTimeout())
				.setRedirectsEnabled(httpClientProperties.isFollowRedirects())
				.build();
		
		HttpClient httpClient = httpClientFactory.createBuilder().
				setConnectionManager(httpClientConnectionManager).
				setDefaultRequestConfig(defaultRequestConfig).
				setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).
				build();
		
		return httpClient;
	}
}
