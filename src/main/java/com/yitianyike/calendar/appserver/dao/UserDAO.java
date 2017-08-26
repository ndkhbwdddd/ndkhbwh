package com.yitianyike.calendar.appserver.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yitianyike.calendar.appserver.model.AuthAccount;
import com.yitianyike.calendar.appserver.model.DeviceInfo;

public interface UserDAO {
	public AuthAccount getAccountInfo(String uid);

	public int SaveAuthAccount(AuthAccount account);

	public List<String> getSubscribeList(String uid);

	public int saveSubscribeId(String uid, String columnId, int type);

	public int delSubscribeId(String uid, String columnId);

	public int delSubscribeIdByType(String uid, int type);

	public List<Map<String, String>> getSubscribedList(String uid);

	public int delSubscribeList(String uid);

	public int saveSubscribe(String uid, Set<Map<String, String>> set);

	public DeviceInfo getDeviceInfo(String uid, String devicetoken);

	public int updateDevicetokenStatus(String uid, String devicetoken, int status);

	public int saveDevicetoken(String uid, String devicetoken, int status);

	public int updateDevicetokenStatusByUid(String uid, int status);
}
