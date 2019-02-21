/**
 * 
 */
package com.beyond.phoenix.router.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONArray;
import com.beyond.phoenix.router.feign.config.FeignConfig;
import com.beyond.phoenix.router.feign.fallback.FeignClientPmOrgFallbackFactory;
import com.beyond.phoenix.router.feign.vo.ResultVO;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午7:21:00
 * +根据千帆组织(广场、门店)查询商管组织
 */
@FeignClient(name = "pmOrgQueryClient",
             url = "${pm.org.query.host}",
             fallbackFactory = FeignClientPmOrgFallbackFactory.class,
             configuration = {FeignConfig.class})
public interface PmOrgQueryClient {
	@RequestMapping(
			method = { RequestMethod.GET }, 
			value = "/phoenix/coral/v1/pmplazaids",
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResultVO<JSONArray> queryPmPlazaInfo(
			@RequestParam(value = "plazaIdList") List<Long> plazaIdList);
	
	@RequestMapping(
			method = { RequestMethod.GET }, 
			value = "/phoenix/coral/v1/pmstoreids",
			consumes = {MediaType.APPLICATION_JSON_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResultVO<JSONArray> queryPmStoreInfo(
			@RequestParam(value = "storeIdList") List<Long> storeIdList);
}
