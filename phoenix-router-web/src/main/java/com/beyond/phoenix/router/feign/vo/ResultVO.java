/**
 * 
 */
package com.beyond.phoenix.router.feign.vo;

import com.beyonds.phoenix.mountain.core.common.model.WdBaseModel;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月30日
 * @Time:   下午7:24:45
 *
 * @param <T>
 */
public class ResultVO<T> extends WdBaseModel {
	private static final long serialVersionUID = -957282774453907220L;
	
	private  int     status;
	private  String  message;
	private  T  data;
	
	public ResultVO() {
		
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
