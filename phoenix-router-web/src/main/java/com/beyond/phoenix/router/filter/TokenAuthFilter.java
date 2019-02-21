/**
 * 
 */
package com.beyond.phoenix.router.filter;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beyond.phoenix.router.enums.ResultEnum;
import com.beyond.phoenix.router.feign.PmOrgQueryClient;
import com.beyond.phoenix.router.feign.TokenCheckClient;
import com.beyond.phoenix.router.feign.vo.ResultVO;
import com.beyond.phoenix.router.model.UriModel;
import com.beyond.phoenix.router.result.ReturnResult;
import com.beyonds.phoenix.mountain.core.common.model.WdBaseModel;
import com.beyonds.phoenix.mountain.core.common.util.LRUMap;
import com.beyonds.phoenix.mountain.core.common.util.LogModel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @Author: DanielCao
 * @Date:   2018年7月28日
 * @Time:   下午5:09:50
 *
 */
public class TokenAuthFilter implements GlobalFilter, Ordered {
	private static final Logger logger = LoggerFactory.getLogger("COMMON.LOG");
	
	private static final String TOKEN = "token";
	private static final String USER_ID = "userId";
	private static final String TENANT_ID = "tenantId";
	private static final String ORG_CODE = "workingOrgCode";
	private static final String ORG_TYPE_CODE = "orgTypeCode";
	
	// 商管相关id
	private static final String PM_PLAZA_ID = "pmPlazaId"; //商管广场id
	private static final String PM_STORE_ID = "pmStoreId"; //商管门店id
	
	private TokenCheckClient tokenCheckClient;
	private PmOrgQueryClient pmOrgQueryClient;
	private boolean checkPmOrg;
	
	// 白名单接口列表,主要是登录、登出和注册接口
	private List<UriModel> whiteUriList;
	// 黑名单接口列表,禁止通过该路由访问
	private List<UriModel> blackUriList;
	private int expireToken = 3600; //缓存时间(秒),默认3600秒
	
	private static final Map<String, CacheEntry> TOKEN_CACHE = new LRUMap<String, CacheEntry>(1000000);
	private static final Map<String, CacheEntry> PLAZA_ID_CACHE = new LRUMap<String, CacheEntry>(1000);
	private static final Map<String, CacheEntry> STORE_ID_CACHE = new LRUMap<String, CacheEntry>(100000);
	
	private int expireTime = 3600; //缓存时间(秒),默认3600秒
	
	public TokenAuthFilter() {
		this(null, null, null, null, false, 3600, 3600);
	}
	
	public TokenAuthFilter(
			TokenCheckClient tokenCheckClient,
			List<UriModel> whiteUriList, 
			List<UriModel> blackUriList,
			PmOrgQueryClient pmOrgQueryClient,
			boolean checkPmOrg, 
			int expireTimeInSec,
			int expireTokenInSec) {
		this.tokenCheckClient = tokenCheckClient;
		this.pmOrgQueryClient = pmOrgQueryClient;
		
		this.whiteUriList = whiteUriList;
		this.blackUriList = blackUriList;
		this.expireToken = expireTokenInSec;
		
		this.checkPmOrg = checkPmOrg;
		this.expireTime = expireTimeInSec;
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		
		LogModel lm = LogModel.newLogModel("TokenAuthFilter.filter");
		lm.addMetaData("method", request.getMethodValue());
		lm.addMetaData("uri", request.getURI());
		lm.addMetaData("params", request.getQueryParams());
		lm.addMetaData("remodeAddress", request.getRemoteAddress());
		logger.info(lm.toJson(false));
		
		ResultEnum enu = null;
		
		//黑名单
		if(isInUriList(request, blackUriList)) {
			exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
			
			enu = ResultEnum.URI_IN_BLACK_LIST;
			DataBuffer buffer = wrapDataBuffer(exchange, enu);
			
			lm.addMetaData("result", enu);
			logger.warn(lm.toJson());
			
			return exchange.getResponse().writeWith(Flux.just(buffer));
		}
		//白名单
		if(isInUriList(request, whiteUriList)) {
			lm.addMetaData("result", ResultEnum.URI_IN_WHITE_LIST);
			logger.warn(lm.toJson());
			
			return chain.filter(exchange);
		}
		
		HttpHeaders headers = request.getHeaders();
		
		String tenantId = headers.getFirst(TENANT_ID);
		lm.addMetaData("tenantId", tenantId);
		if(tenantId == null || tenantId.trim().equals("")) {
			tenantId = "2018092600001";
		}
		String workingOrgCode = headers.getFirst(ORG_CODE);
		String token = headers.getFirst(TOKEN);
		String userId = headers.getFirst(USER_ID);
		String orgTypeCode = headers.getFirst(ORG_TYPE_CODE);
		
		// 商管相关id
		String pmPlazaId = headers.getFirst(PM_PLAZA_ID);
		String pmStoreId = headers.getFirst(PM_STORE_ID);
		
		lm.addMetaData("userId", userId);
		lm.addMetaData("token", token);
		lm.addMetaData("workingOrgCode", workingOrgCode);
		lm.addMetaData("orgTypeCode", orgTypeCode);
		
		lm.addMetaData("pmPlazaId", pmPlazaId);
		lm.addMetaData("pmStoreId", pmStoreId);
		
		boolean hasPmOrg = false;
		if (checkPmOrg) {
			if (StringUtils.isEmpty(pmPlazaId) && StringUtils.isEmpty(pmStoreId)) {
				hasPmOrg = false;
			} else {
				hasPmOrg = true;
			}
		} else {
			hasPmOrg = true;
		}
		
		
		// 参数判空
		if (StringUtils.isEmpty(token) || 
		   StringUtils.isEmpty(workingOrgCode) || 
		   StringUtils.isEmpty(orgTypeCode) || 
		   StringUtils.isEmpty(tenantId) ||
		   StringUtils.isEmpty(userId) || 
		   (!hasPmOrg)) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			
			enu = ResultEnum.AUTHORIZE_PARAM_NULL_ERROR;
			DataBuffer buffer = wrapDataBuffer(exchange, enu);
			
			lm.addMetaData("result", enu);
			logger.error(lm.toJson());
			
			return exchange.getResponse().writeWith(Flux.just(buffer));
		}
		
		// 参数是否合法,调用token校验接口
		boolean checkOK = false;
		try {
			Long _userId = Long.parseLong(userId.trim());
			Long _tenantId = Long.parseLong(tenantId.trim());
			
			// 检查缓存
			String cacheKey = "token_" + _userId + "_" + _tenantId + "_" + workingOrgCode;
			CacheEntry _tokenCache = TOKEN_CACHE.get(cacheKey);
			if (_tokenCache != null) {
				if (_tokenCache.hasValue(token) && (!_tokenCache.isExpired())) {
					checkOK = true;
				}
			}
			if (!checkOK) {
				ResultVO<Boolean> checkResult = tokenCheckClient.checkToken(_userId, _tenantId, 
						token, workingOrgCode, orgTypeCode);
				if(checkResult != null && 
				   checkResult.getData() != null && 
				   checkResult.getData().booleanValue() == true) {
					_tokenCache = new CacheEntry(cacheKey, System.currentTimeMillis() + 1000L * expireToken);
					TOKEN_CACHE.put(cacheKey, _tokenCache);
					
					checkOK = true;
				}else {
					checkOK = false;
				}
			}
		} catch(Exception e) {
			checkOK = false;
			
			logger.info(e.getMessage());
			
			e.printStackTrace();
		}
		if(!checkOK) {
			exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
			
			enu = ResultEnum.AUTHORIZE_FAIL_ERROR;
			DataBuffer buffer = wrapDataBuffer(exchange, enu);
			
			lm.addMetaData("result", enu);
			logger.error(lm.toJson());
			
			return exchange.getResponse().writeWith(Flux.just(buffer));
		}
		// 检查组织是否匹配
		boolean _checkPmOrg = false;
		if (this.checkPmOrg) {
			_checkPmOrg = chackPmOrg(workingOrgCode, pmPlazaId, pmStoreId);
		} else {
			_checkPmOrg = true;
		}
		if (!_checkPmOrg) {
			exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
			
			enu = ResultEnum.AUTHORIZE_FAIL_ERROR;
			DataBuffer buffer = wrapDataBuffer(exchange, enu);
			
			lm.addMetaData("result", enu);
			logger.error(lm.toJson());
			
			return exchange.getResponse().writeWith(Flux.just(buffer));
		}
        
		enu = ResultEnum.SUCCESS;
		lm.addMetaData("result", enu);
		logger.info(lm.toJson());
		
		return chain.filter(exchange);
	}
	
	@Override
	public int getOrder() {
		return -10000;
	}
	
	/**
	 * +判定组织id与商管相关id是否匹配
	 * @param workingOrgCode 千帆小程序组织id
	 * @param pmPlazaId 商管广场id
	 * @param pmStoreId 商管门店id
	 * @return 是否匹配
	 */
	private boolean chackPmOrg(String workingOrgCode, String pmPlazaId, String pmStoreId) {
		List<Long> idList = new ArrayList<Long>();
		long _workingOrgId = Long.parseLong(workingOrgCode);
		idList.add(_workingOrgId);
		
		// 检查pmPlazaId
		if (!StringUtils.isEmpty(pmPlazaId)) {
			// 查询缓存
			String cacheKey = "plaza_" + workingOrgCode;
			CacheEntry pmPlazaCache = PLAZA_ID_CACHE.get(cacheKey);
			if (pmPlazaCache != null ) {
				if (pmPlazaCache.hasValue(pmPlazaId) && (!pmPlazaCache.isExpired())) {
					return true;
				}
			}
			
			ResultVO<JSONArray> plazaResult = pmOrgQueryClient.queryPmPlazaInfo(idList);
			if (plazaResult != null && plazaResult.getStatus() == 200) {
				JSONArray jsonArrayResult = plazaResult.getData();
				if (jsonArrayResult != null && !jsonArrayResult.isEmpty()) {
					JSONObject jsonObject = jsonArrayResult.getJSONObject(0);
					String dataKey = jsonObject.getString("dataKey");
					String dataValue = jsonObject.getString("dataValue");
					if ((!StringUtils.isEmpty(dataKey)) && dataKey.equals(workingOrgCode) &&
						(!StringUtils.isEmpty(dataValue)) && dataValue.equals(pmPlazaId)) {
						pmPlazaCache = new CacheEntry(cacheKey, System.currentTimeMillis() + 1000L * expireTime);
						PLAZA_ID_CACHE.put(cacheKey, pmPlazaCache);
						
						return true;
					}
				}
			}
		}
		
		// 检查pmStoreId
		if (!StringUtils.isEmpty(pmStoreId)) {
			// 查询缓存
			String cacheKey = "store_" + workingOrgCode;
			CacheEntry pmStoreCache = STORE_ID_CACHE.get(cacheKey);
			if (pmStoreCache != null ) {
				if (pmStoreCache.hasValue(pmStoreId) && (!pmStoreCache.isExpired())) {
					return true;
				}
			}
			
			ResultVO<JSONArray> storeResult = pmOrgQueryClient.queryPmStoreInfo(idList);
			if (storeResult != null && storeResult.getStatus() == 200) {
				JSONArray jsonArrayResult = storeResult.getData();
				if (jsonArrayResult != null && !jsonArrayResult.isEmpty()) {
					JSONObject jsonObject = jsonArrayResult.getJSONObject(0);
					String dataKey = jsonObject.getString("dataKey");
					String dataValue = jsonObject.getString("dataValue");
					if ((!StringUtils.isEmpty(dataKey)) && dataKey.equals(workingOrgCode) &&
						(!StringUtils.isEmpty(dataValue)) && dataValue.equals(pmStoreId)) {
						pmStoreCache = new CacheEntry(cacheKey, System.currentTimeMillis() + 1000L * expireTime);
						STORE_ID_CACHE.put(cacheKey, pmStoreCache);
						
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean isInUriList(final ServerHttpRequest request, final List<UriModel> blackUriList) {
		try {
			URI uri = request.getURI();
			String method = (request.getMethodValue() == null? "" : request.getMethodValue());
			String path = (uri == null? "" : (uri.getPath() == null? "" : uri.getPath()));
			if(blackUriList != null && !blackUriList.isEmpty()) {
				for(UriModel uriModel : blackUriList) {
					if(method.equalsIgnoreCase(uriModel.getMethod()) && checkUri(path, uriModel.getUri())) {
						return true;
					}
				}
			}
		}catch(Exception e) {
			logger.error("", e);
		}
		
		return false;
	}
	
	private boolean checkUri(final String path, final String pathRegex) {
		if(path.equalsIgnoreCase(pathRegex)) {
			return true;
		}
		
		String _path = path.toLowerCase();
		String _pathRegex = pathRegex.toLowerCase();
		if(_path.matches("^" + _pathRegex + "$")) {
			return true;
		}
		
		return false;
	}
	
	private DataBuffer wrapDataBuffer(ServerWebExchange exchange, ResultEnum enu) {
		ReturnResult<Object> resultEntry = new ReturnResult<>(enu.getCode(), enu.getDesc());
		byte[] bytes = JSONObject.toJSONString(resultEntry).getBytes(StandardCharsets.UTF_8);
		
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
		
		return exchange.getResponse().bufferFactory().wrap(bytes);
	}
	
	static class CacheEntry extends WdBaseModel {
		private static final long serialVersionUID = 5021812454323106493L;
		
		private String value;
		private long   expiredTime = 0L; //过期时间
		
		public CacheEntry(String value, Long expiredTime) {
			this.value = value;
			this.expiredTime = expiredTime;
		}
		
		/**
		 * +判断是否过期
		 * @return
		 */
		public boolean isExpired() {
			return System.currentTimeMillis() > this.expiredTime;
		}
		
		/**
		 * +判断该值是否相等
		 * @param destValue
		 * @return
		 */
		public boolean hasValue(String destValue) {
			if ((!StringUtils.isEmpty(value)) && (!StringUtils.isEmpty(destValue)) && value.equals(destValue)) {
				return true;
			}
			
			return false;
		}

		public String getValue() {
			return value;
		}

		public Long getExpiredTime() {
			return expiredTime;
		}
	}
}
