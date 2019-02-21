/**
 * 
 */
package com.beyond.phoenix.router.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.beyond.phoenix.router.feign.config.FeignConfig;
import com.beyond.phoenix.router.feign.fallback.FeignClientFallbackFactory;
import com.beyond.phoenix.router.feign.vo.ResultVO;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午7:21:00
 *
 */
@FeignClient(name = "tokenCheckClient",
             url = "${voyager.token.host}",
             fallbackFactory = FeignClientFallbackFactory.class,
             configuration = {FeignConfig.class})
public interface TokenCheckClient {
	@RequestMapping(
			method = { RequestMethod.GET }, 
			value = "/permission/voyager/basisb/v1/token/{userId}",
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResultVO<Boolean> checkToken(
			@PathVariable(value = "userId") Long userId, 
			@RequestHeader(value = "tenantId", required = false, defaultValue = "2018092600001") Long tenantId, 
			@RequestHeader(value = "token") String token, 
			@RequestHeader(value = "workingOrgCode") String workingOrgCode,
			@RequestHeader(value = "orgTypeCode") String orgTypeCode);
}
