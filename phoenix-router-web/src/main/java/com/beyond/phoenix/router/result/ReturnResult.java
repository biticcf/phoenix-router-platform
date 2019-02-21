/**
 *
 */
package com.beyond.phoenix.router.result;

import java.io.Serializable;

/**
 * 
 * @Author: DanielCao
 * @Date:   2018年7月28日
 * @Time:   下午8:28:51
 *
 * @param <T>
 */
public class ReturnResult<T> implements Serializable {
	private static final long serialVersionUID = -8379100651362895882L;
	
	private   int       status;
	private   String    message;
	private   T         data;
	
	public ReturnResult(){
		this(0);
	}
	
	public ReturnResult(int status){
		this(status, null);
	}
	
	public ReturnResult(int status, String message){
		this(status, message, null);
	}
	
	public ReturnResult(int status, String message, T data){
		this.status = status;
		this.message = message;
		this.data = data;
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
