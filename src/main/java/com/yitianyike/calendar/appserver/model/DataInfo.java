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
public class DataInfo {
	
	private String cacheKey;
	private String field;
	private String cacheValue;
	
	private Long updateTime;
	private Long createTime;
	public String getCacheKey() {
		return cacheKey;
	}
	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getCacheValue() {
		return cacheValue;
	}
	public void setCacheValue(String cacheValue) {
		this.cacheValue = cacheValue;
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	
	
	
}
