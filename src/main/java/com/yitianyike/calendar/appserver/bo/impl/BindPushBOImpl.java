/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.BindPushBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.DeviceInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("bindPushBO")
public class BindPushBOImpl implements BindPushBO {

	private static Logger logger = Logger.getLogger(BindPushBOImpl.class.getName());
	@Autowired
	private UserDAO userDAO;
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String devicetoken = map.get("devicetoken");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationUidAndToken(token)
					|| !ParameterValidation.validationDevicetoken(devicetoken)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			int redisIndex = CalendarUtil.getRedisIndex(token);
			String redisTemplate = "redisTemplate" + redisIndex;
			redisDAO.setRedisTemplate(redisTemplate);

			Map<String, String> tokenMap = redisDAO.hGetAll(token);

			String uid = tokenMap.get("uid");
			if (uid != null) {
				// 查询uid和devicetoken是否存在于缓存中,是返回已经绑定
				// 否查询mysql是否存在并为1,是写入缓存中
				// 并绑定
				// String devicetokens = redisDAO.hGetValue("device" + uid,
				// "devicetoken");
				int exist = redisDAO.sSetValueExist("device" + uid, devicetoken);
				Map<String, String> resultmap = new HashMap<String, String>();
				// 不为空并且包含此token,跳出已经绑定
				if (exist == 1) {
					resultmap.put("code", "1");
					resultmap.put("status", EnumConstants.CALENDAR_BIND_PUSH_OK);
					appResponse.setRespContent(JSON.toJSONString(resultmap));
					redisDAO.clearRedisTemplate();
					break;
				}

				// 查看数据库中是否绑定
				int dbIndex = CalendarUtil.getDbIndex(uid);
				String dataSource = "dataSource" + dbIndex;
				DBContextHolder.setDBType(dataSource);
				DeviceInfo deviceInfo = userDAO.getDeviceInfo(uid, devicetoken);

				if (deviceInfo != null && deviceInfo.getStatus() == 0) {
					// 修改
					userDAO.updateDevicetokenStatus(uid, devicetoken, 1);
				}
				if (deviceInfo == null) {
					// 为空
					userDAO.saveDevicetoken(uid, devicetoken, 1);
				}
				DBContextHolder.clearDBType();
				// 写入缓存中

				redisDAO.sAddSetValue("device" + uid, devicetoken);
				// appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);
				resultmap.put("code", "1");
				resultmap.put("status", EnumConstants.CALENDAR_BIND_PUSH_OK);
				appResponse.setRespContent(JSON.toJSONString(resultmap));
			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");

			}
			redisDAO.clearRedisTemplate();
		} while (false);

		return appResponse;
	}

}
