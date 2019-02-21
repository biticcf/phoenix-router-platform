/**
 * 
 */
package com.beyond.phoenix.router.feign.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.beyond.phoenix.router.enums.ResultEnum;
import com.beyond.phoenix.router.feign.TokenCheckClient;
import com.beyond.phoenix.router.feign.vo.ResultVO;

import feign.hystrix.FallbackFactory;

/**
 * @Author: DanielCao
 * @Date:   2018年9月5日
 * @Time:   下午5:09:56
 *
 */
@Component(value = "fb")
public class FeignClientFallbackFactory implements FallbackFactory<TokenCheckClient> {
	private static final Logger logger = LoggerFactory.getLogger(FeignClientFallbackFactory.class);
	
	@Override
	public TokenCheckClient create(Throwable cause) {
		return new TokenCheckClient() {
			@Override
			public ResultVO<Boolean> checkToken(Long userId, Long tenantId, 
					String token, String workingOrgCode, String orgTypeCode) {
				ResultVO<Boolean> rv = new ResultVO<>();
				
				logger.error("调用权限校验接口失败", cause);
				
				ResultEnum enu = ResultEnum.CHECK_TOKEN_SERVER_ERROR;
				rv.setData(false);
				rv.setStatus(enu.getCode());
				rv.setMessage(cause.getMessage());
				
				return rv;
			}
			
		};
	}
	
}
