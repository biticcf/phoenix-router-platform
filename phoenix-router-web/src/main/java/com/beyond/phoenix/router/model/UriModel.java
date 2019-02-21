/**
 * 
 */
package com.beyond.phoenix.router.model;

import com.beyonds.phoenix.mountain.core.common.model.WdBaseModel;

/**
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   上午9:31:46
 *
 */
public class UriModel extends WdBaseModel {
	private static final long serialVersionUID = -4748007587770421468L;
	
	private String uri;
	private String method;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
}
