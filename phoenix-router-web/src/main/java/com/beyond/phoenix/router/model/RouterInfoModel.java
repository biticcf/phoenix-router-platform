/**
 * 
 */
package com.beyond.phoenix.router.model;

import java.net.URI;

import com.beyonds.phoenix.mountain.core.common.model.WdBaseModel;

/**
 * @Author: DanielCao
 * @Date:   2018年8月31日
 * @Time:   下午3:32:30
 *
 */
public class RouterInfoModel extends WdBaseModel {
	private static final long serialVersionUID = 6541392666259808602L;
	
	private String  id;
	private String  name;
	private URI     uri;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public URI getUri() {
		return uri;
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}
}
