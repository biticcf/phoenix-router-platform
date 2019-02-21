/**
 *
 */
package com.beyond.phoenix.router.enums;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   上午8:57:32
 * 错误码范围：1100xxxxx
 */
public enum ResultEnum {
	SUCCESS(200, "SUCCESS", "成功"),
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "内部服务异常"),
	
	/**
	 * 参数错误110004xxx
	 */
	PARAM_ERROR(110004000, "PARAM_ERROR", "参数错误"),
	PARAM_GOODS_CODE_LENGTH_ERROR(110004001, "PARAM_LENGTH_ERROR", "goodsCode参数长度错误"),
	PARAM_GOODS_CODE_NULL_ERROR(110004002, "PARAM_GOODS_CODE_NULL_ERROR", "goodsCode参数不允许空"),
	PARAM_SIZE_RANGE_ERROR(110004003, "PARAM_SIZE_RANGE_ERROR", "size参数取值范围错误"),
	PARAM_SIZE_NULL_ERROR(110004004, "PARAM_SIZE_NULL_ERROR", "size参数不允许空"),
	PARAM_HEADER_ERROR(110004005, "PARAM_HEADER_ERROR", "header参数错误"),
	
	/**
	 * 业务错误110005xxx
	 */
	SYS_ERROR(110005000, "SYS_ERROR", "系统异常"),
	CHECK_TOKEN_SERVER_ERROR(110005001, "CHECK_TOKEN_SERVER_ERROR", "Token检验接口错误"),
	QUERY_MP_PLAZA_SERVER_ERROR(110005002, "QUERY_MP_PLAZA_SERVER_ERROR", "查询商管广场信息接口错误"),
	QUERY_MP_STORE_SERVER_ERROR(110005003, "QUERY_MP_STORE_SERVER_ERROR", "查询商管门店信息接口错误"),
	
	/**
	 * 登录认证110006xxx
	 */
	TOKEN_NULL_ERROR(110006000, "TOKEN_NULL_ERROR", "token不允许空"),
	ORG_NULL_ERROR(110006001, "ORG_NULL_ERROR", "workingOrgCode不允许空"),
	TENANT_NULL_ERROR(110006002, "TENANT_NULL_ERROR", "tenantId不允许空"),
	AUTHORIZE_FAIL_ERROR(110006003, "AUTHORIZE_FAIL_ERROR", "用户认证失败"),
	REQUEST_NOT_ALLOWED_ERROR(110006004, "REQUEST_NOT_ALLOWED_ERROR", "该请求禁止访问"),
	AUTHORIZE_PARAM_NULL_ERROR(110006005, "AUTHORIZE_PARAM_NULL_ERROR", "用户认证参数缺失"),
	URI_IN_WHITE_LIST(110006006, "URI_IN_WHITE_LIST", "白名单uri"),
	URI_IN_BLACK_LIST(110006007, "URI_IN_BLACK_LIST", "黑名单uri"),
	
	/**
	 * 系统错误110009xxx
	 */
	DB_ERROR(110009997, "DB_ERROR", "数据库异常"),
	FAILURE(110009998, "FAILURE", "业务失败"),
	UNKNOWN(110009999, "UNKNOWN", "未定义错误");

	private int code;
	private String message;
	private String desc;

	private ResultEnum(int code, String message, String desc){
		this.code = code;
		this.message = message;
		this.desc = desc;
	}
	
	public static ResultEnum valueOf(int code){
		ResultEnum[] enums = values();
		if(enums == null || enums.length == 0){
			return UNKNOWN;
		}
		for(ResultEnum _enu : enums){
			if(code == _enu.getCode()){
				return _enu;
			}
		}
		
		return UNKNOWN;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		try {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			        .append("code", code)
					.append("message", message)
					.append("desc", desc)
					.toString();
        } catch (Exception e) {
            // NOTICE: 这样做的目的是避免由于toString()的异常导致系统异常终止
            // 大部分情况下，toString()用在日志输出等调试场景
            return super.toString();
        }
	}
}
