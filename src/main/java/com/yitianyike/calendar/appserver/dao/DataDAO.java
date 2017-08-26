package com.yitianyike.calendar.appserver.dao;

import java.util.List;

import com.yitianyike.calendar.appserver.model.DataInfo;




public interface DataDAO {
	
	public List<DataInfo> getDataInfos(String key);
	
}
