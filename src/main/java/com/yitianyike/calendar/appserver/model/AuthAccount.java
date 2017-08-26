/**
 * 
 */
package com.yitianyike.calendar.appserver.model;


/**
 * 用户认证表对象
 * 
 * @author xujinbo
 *
 */
public class AuthAccount {
	
	private String uid;
	private String channelCode;
	/**
	 * 用户状态（0-停用，1-正在使用）
	 */
	private int status;
	private Long createTime;
	
	
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	
	
}
