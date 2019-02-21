/**
 * 
 */
package com.beyond.phoenix.router.feign.fallback;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.beyond.phoenix.router.enums.ResultEnum;
import com.beyond.phoenix.router.feign.PmOrgQueryClient;
import com.beyond.phoenix.router.feign.vo.ResultVO;

import feign.hystrix.FallbackFactory;

/**
 * @Author: DanielCao
 * @Date:   2018年9月5日
 * @Time:   下午5:09:56
 *
 */
@Component(value = "fbPmorg")
public class FeignClientPmOrgFallbackFactory implements FallbackFactory<PmOrgQueryClient> {
	private static final Logger logger = LoggerFactory.getLogger(FeignClientPmOrgFallbackFactory.class);
	
	@Override
	public PmOrgQueryClient create(Throwable cause) {
		return new PmOrgQueryClient() {
			
			@Override
			public ResultVO<JSONArray> queryPmPlazaInfo(List<Long> plazaIdList) {
				ResultVO<JSONArray> rv = new ResultVO<>();
				
				logger.error("调用商管广场信息查询接口失败", cause);
				
				ResultEnum enu = ResultEnum.QUERY_MP_PLAZA_SERVER_ERROR;
				rv.setData(null);
				rv.setStatus(enu.getCode());
				rv.setMessage(cause.getMessage());
				
				return rv;
			}

			@Override
			public ResultVO<JSONArray> queryPmStoreInfo(List<Long> storeIdList) {
				ResultVO<JSONArray> rv = new ResultVO<>();
				
				logger.error("调用商管门店信息查询接口失败", cause);
				
				ResultEnum enu = ResultEnum.QUERY_MP_STORE_SERVER_ERROR;
				rv.setData(null);
				rv.setStatus(enu.getCode());
				rv.setMessage(cause.getMessage());
				
				return rv;
			}
			
		};
	}
	
}
