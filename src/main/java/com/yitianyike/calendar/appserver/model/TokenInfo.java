/**
 * 
 */
package com.yitianyike.calendar.appserver.model;

import java.util.List;


/**
 * 设备信息对象
 * 
 * @author xujinbo
 *
 */
public class TokenInfo {
	
	private String token;
	
	private String uid;
	
	private String channelCode;
	
	private String version;
	
	private int time;
	
	private List<String> columnList;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public List<String> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<String> columnList) {
		this.columnList = columnList;
	}
	
	
	
}
