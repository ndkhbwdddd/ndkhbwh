package com.yitianyike.calendar.appserver.dao;


import com.yitianyike.calendar.appserver.model.RegisterInfo;

public interface RegisterDAO {
	
	public RegisterInfo getRegisterInfo(String userId, String field);
	
	public int updateThirdIdByUuid(String thirdId, String uuid);
	
	public int saveRegisterInfo(RegisterInfo registerInfo);

	public int deleteRegisterInfoByUUID(String uuid);
}
