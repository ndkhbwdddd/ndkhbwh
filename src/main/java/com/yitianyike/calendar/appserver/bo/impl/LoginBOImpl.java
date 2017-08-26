/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.LoginBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.AuthAccount;
import com.yitianyike.calendar.appserver.model.DeviceInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;
import com.yitianyike.calendar.appserver.util.PropertiesUtil;

/**
 * @author xujinbo
 *
 */
@Component("loginBO")
public class LoginBOImpl implements LoginBO {

	private static Logger logger = Logger.getLogger(LoginBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		return multiLoginProcess(map, content, requestIndex);
	}

	public AppResponse singleLoginProcess(Map<String, String> map, String content, long requestIndex) {
		String mobile = map.get("telephone");
		String uid = map.get("uid");
		String channelCode = map.get("channelno");
		String version = map.get("version");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationChannelno(channelCode) || !ParameterValidation.validationVersion(version)
					|| !ParameterValidation.validationUidAndToken(uid)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			if (ParameterValidation.validationMobile(mobile)) {/// 通过手机号码登录
				// boolean result = mobile.matches("[0-9]+");
				// if (result == false) {
				// appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				// logger.error(requestIndex + " : param error 2, return 400");
				// break;
				// }
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_501);
				logger.info(requestIndex + " : user login by mobile, no support, return 501");

			} else if (uid != null) {/// 通过uid登录

				Map<String, String> uidMap = new HashMap<String, String>();
				// tokenMap.put("uid", uid);
				uidMap.put("channel", channelCode);
				uidMap.put("version", version);

				int redisIndex = CalendarUtil.getRedisIndex(uid);
				String redisTemplate = "redisTemplate" + redisIndex;
				redisDAO.setRedisTemplate(redisTemplate);
				String oldToken = redisDAO.hGetValue(uid, "token");

				// if (oldToken != null) {
				// Map<String, String> tempMap = redisDAO.hGetAll(oldToken);
				// if (tempMap.get("list") != null) {
				// tokenMap.put("list", tempMap.get("list"));
				// } else {
				// logger.info(requestIndex + " : old token(" + oldToken + ")
				// list is null");
				// }
				// }
				if (oldToken == null) {
					int dbIndex = CalendarUtil.getDbIndex(uid);
					String dataSource = "dataSource" + dbIndex;
					DBContextHolder.setDBType(dataSource);

					AuthAccount authAccount = userDAO.getAccountInfo(uid);
					if (authAccount == null) {
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
						logger.error(requestIndex + " : uid no exist, return 401");
						redisDAO.clearRedisTemplate();
						DBContextHolder.clearDBType();
						break;
					}

					List<String> columnList = userDAO.getSubscribeList(uid);
					String columnString = "";
					if (columnList != null) {
						for (String id : columnList) {
							columnString += id + ",";
						}
					}

					uidMap.put("list", columnString);
				}

				String token = CalendarUtil.generateToken(uid);
				uidMap.put("token", token);
				redisDAO.processTokenAndUid(uid, token, oldToken, uidMap, "" + PropertiesUtil.tokenTime);
				redisDAO.clearRedisTemplate();

				DBContextHolder.clearDBType();

				Map<String, String> loginMap = new HashMap<String, String>();
				loginMap.put("token", token);
				loginMap.put("tokenTime", PropertiesUtil.tokenTime + "");
				loginMap.put("encryptFlag", PropertiesUtil.encryptFlag + "");
				appResponse.setRespContent(JSON.toJSONString(loginMap));
				logger.info(requestIndex + " : user login successfully, return token = " + token);

			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}

	public AppResponse multiLoginProcess(Map<String, String> map, String content, long requestIndex) {
		String mobile = map.get("telephone");
		String uid = map.get("uid");
		String channelCode = map.get("channelno");
		String version = map.get("version");

		String olduid = map.get("olduid");
		String devicetoken = map.get("devicetoken");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationChannelno(channelCode) || !ParameterValidation.validationVersion(version)
					|| !ParameterValidation.validationUidAndToken(uid)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			if (ParameterValidation.validationMobile(mobile)) {/// 通过手机号码登录
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_501);
				logger.info(requestIndex + " : user login by mobile, no support, return 501");

			} else if (uid != null) {/// 通过uid登录

				Map<String, String> uidMap = new HashMap<String, String>();
				uidMap.put("channel", channelCode);
				uidMap.put("version", version);

				int redisIndex = CalendarUtil.getRedisIndex(uid);
				String redisTemplate = "redisTemplate" + redisIndex;
				redisDAO.setRedisTemplate(redisTemplate);
				String oldToken = redisDAO.hGetValue(uid, "token");
				if (oldToken == null) {
					int dbIndex = CalendarUtil.getDbIndex(uid);
					String dataSource = "dataSource" + dbIndex;
					DBContextHolder.setDBType(dataSource);

					AuthAccount authAccount = userDAO.getAccountInfo(uid);
					if (authAccount == null) {
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
						logger.error(requestIndex + " : uid no exist, return 401");
						redisDAO.clearRedisTemplate();
						DBContextHolder.clearDBType();
						break;
					}

					List<String> columnList = userDAO.getSubscribeList(uid);
					String columnString = "";
					if (columnList != null) {
						for (String id : columnList) {
							columnString += id + ",";
						}
					}

					uidMap.put("list", columnString);
				}

				String token = CalendarUtil.generateToken(uid);
				uidMap.put("token", token);
				redisDAO.processTokenAndUidForMulti(uid, token, oldToken, uidMap, "" + PropertiesUtil.tokenTime);
				redisDAO.clearRedisTemplate();

				DBContextHolder.clearDBType();

				Map<String, String> loginMap = new HashMap<String, String>();
				loginMap.put("token", token);
				loginMap.put("tokenTime", PropertiesUtil.tokenTime + "");
				loginMap.put("encryptFlag", PropertiesUtil.encryptFlag + "");
				appResponse.setRespContent(JSON.toJSONString(loginMap));
				logger.info(requestIndex + " : user login successfully, return token = " + token);

				// 推送处理
				if (olduid != null && devicetoken != null) {
					// 查看旧id是否绑定
					int olduidredisIndex = CalendarUtil.getRedisIndex(olduid);
					String olduidredisTemplate = "redisTemplate" + olduidredisIndex;
					redisDAO.setRedisTemplate(olduidredisTemplate);
					int valueExist = redisDAO.sSetValueExist("device" + olduid, devicetoken);
					if (valueExist != 0) {
						// 解绑旧id
						redisDAO.delSetValue("device" + olduid, devicetoken);
						int dbIndex = CalendarUtil.getDbIndex(olduid);
						String dataSource = "dataSource" + dbIndex;
						DBContextHolder.setDBType(dataSource);
						// 解绑
						userDAO.updateDevicetokenStatus(olduid, devicetoken, 0);
						DBContextHolder.clearDBType();
					}
					redisDAO.clearRedisTemplate();
					// 绑定新id
					int uidredisIndex = CalendarUtil.getRedisIndex(uid);
					String uidredisTemplate = "redisTemplate" + uidredisIndex;
					redisDAO.setRedisTemplate(uidredisTemplate);
					int newValueExist = redisDAO.sSetValueExist("device" + uid, devicetoken);
					if (newValueExist == 0) {
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
					}
					redisDAO.clearRedisTemplate();
				}

			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}

}
