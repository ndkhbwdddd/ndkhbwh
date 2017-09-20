/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import com.yitianyike.calendar.appserver.model.UserSub;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.MapUtil;
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

				// 查看用户是否有效
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
				}

				// 查询用户订阅项目,包含有效和无效订阅项
				List<UserSub> columnList = userDAO.getSubscribeListIncludeType(uid);
				Set<String> validsubs = new HashSet<String>();
				Set<String> futilitysubs = new HashSet<String>();
				for (UserSub userSub : columnList) {
					String column_id = userSub.getColumn_id();
					if ("0".equals(userSub.getType())) {
						futilitysubs.add(column_id);
					} else {
						validsubs.add(column_id);
					}
				}

				String default_string = uidMap.get("channel") + "-" + uidMap.get("version") + "-default-sub";
				String default_aidlist = redisDAO.hGetValue(default_string, default_string);
				if (default_aidlist!=null&&default_aidlist.length() > 2) {
					List<String> default_aids_all = Arrays
							.asList(default_aidlist.substring(1, default_aidlist.length() - 1).split(","));
					Set<String> default_aids_valid = new HashSet<String>();
					for (String aid : default_aids_all) {
						if (!validsubs.contains(aid) && !futilitysubs.contains(aid)) {
							default_aids_valid.add(aid);
						}
					}
					// 将有效默认项订阅,然后放入到用户订阅缓存中
					if (!default_aids_valid.isEmpty()) {
						userDAO.batchSaveSubscribeId(uid, default_aids_valid, 1);
						validsubs.addAll(default_aids_valid);
					}
				}

				
				//排序
//				String key = uidMap.get("channel")+"-"+uidMap.get("version")+"-subscribed";
//
//				String field =uidMap.get("channel")+"-"+uidMap.get("version")+"-subscribed-order";
//				String aidsOrderString = redisDAO.hGetValue(key, field);
//				// orderMap
//				Map<String, String> orderMap = new HashMap<String, String>();
//				if (StringUtils.isNotBlank(aidsOrderString)) {
//					List<String> aid_orders = java.util.Arrays.asList(aidsOrderString.split(","));
//					for (String aid_order : aid_orders) {
//						String[] aidOrder = aid_order.split("-");
//						if (aidOrder.length == 2) {
//							orderMap.put(aidOrder[0], aidOrder[1]);
//						}
//					}
//				}
				// 用户订阅ordermap
//				Map<String, Integer> useSubOrderMap = new HashMap<String, Integer>();
//				for (String aid : validsubs) {
//					if (orderMap.containsKey(aid)) {
//						String orderNum = orderMap.get(aid);
//						useSubOrderMap.put(aid, Integer.parseInt(orderNum));
//					} else {
//						useSubOrderMap.put(aid, 999);
//					}
//				}
//				List<String> sortColumnList = MapUtil.sortByValueAsc(useSubOrderMap);
				
				
				
				
				String columnString = "";
				if (validsubs != null) {
					for (String id : validsubs) {
						columnString += id + ",";
					}
				}
				uidMap.put("list", columnString);
				
				// 无论是否过期都生成新token
				String token = CalendarUtil.generateToken(uid);
				uidMap.put("token", token);
				redisDAO.processTokenAndUidAndSubsForMulti(uid, token,uidMap, "" + PropertiesUtil.tokenTime);
				redisDAO.clearRedisTemplate();

				DBContextHolder.clearDBType();

				Map<String, String> loginMap = new HashMap<String, String>();
				loginMap.put("token", token);
				loginMap.put("tokenTime", PropertiesUtil.tokenTime + "");
				loginMap.put("encryptFlag", PropertiesUtil.encryptFlag + "");
				appResponse.setRespContent(JSON.toJSONString(loginMap));
				logger.info(requestIndex + " : user login successfully, return token = " + token);

				
				// 获取默认订阅项

				// 无默认订阅项处理
				// Map<String, String> uidMap = new HashMap<String, String>();
				// uidMap.put("channel", channelCode);
				// uidMap.put("version", version);

				// int redisIndex = CalendarUtil.getRedisIndex(uid);
				// String redisTemplate = "redisTemplate" + redisIndex;
				// redisDAO.setRedisTemplate(redisTemplate);
				// String oldToken = redisDAO.hGetValue(uid, "token");
				// 如果没有token可能是第一次登录也可能是token过期了
				// 判断uid是否为空
				// 从数据中获取用户订阅项
//				if (oldToken == null) {
//					int dbIndex = CalendarUtil.getDbIndex(uid);
//					String dataSource = "dataSource" + dbIndex;
//					DBContextHolder.setDBType(dataSource);
//
//					AuthAccount authAccount = userDAO.getAccountInfo(uid);
//					if (authAccount == null) {
//						appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
//						logger.error(requestIndex + " : uid no exist, return 401");
//						redisDAO.clearRedisTemplate();
//						DBContextHolder.clearDBType();
//						break;
//					}
//
//					//
//					List<String> columnList = userDAO.getSubscribeList(uid);
//					String columnString = "";
//					if (columnList != null) {
//						for (String id : columnList) {
//							columnString += id + ",";
//						}
//					}
//
//					uidMap.put("list", columnString);
//				}

				// 无论是否过期都生成新token
//				String token = CalendarUtil.generateToken(uid);
//				uidMap.put("token", token);
//				redisDAO.processTokenAndUidForMulti(uid, token, oldToken, uidMap, "" + PropertiesUtil.tokenTime);
//				redisDAO.clearRedisTemplate();
//
//				DBContextHolder.clearDBType();
//
//				Map<String, String> loginMap = new HashMap<String, String>();
//				loginMap.put("token", token);
//				loginMap.put("tokenTime", PropertiesUtil.tokenTime + "");
//				loginMap.put("encryptFlag", PropertiesUtil.encryptFlag + "");
//				appResponse.setRespContent(JSON.toJSONString(loginMap));
//				logger.info(requestIndex + " : user login successfully, return token = " + token);

				// 推送处理
				// if (olduid != null && devicetoken != null) {
				// // 查看旧id是否绑定
				// int olduidredisIndex = CalendarUtil.getRedisIndex(olduid);
				// String olduidredisTemplate = "redisTemplate" +
				// olduidredisIndex;
				// redisDAO.setRedisTemplate(olduidredisTemplate);
				// int valueExist = redisDAO.sSetValueExist("device" + olduid,
				// devicetoken);
				// if (valueExist != 0) {
				// // 解绑旧id
				// redisDAO.delSetValue("device" + olduid, devicetoken);
				// int dbIndex = CalendarUtil.getDbIndex(olduid);
				// String dataSource = "dataSource" + dbIndex;
				// DBContextHolder.setDBType(dataSource);
				// // 解绑
				// userDAO.updateDevicetokenStatus(olduid, devicetoken, 0);
				// DBContextHolder.clearDBType();
				// }
				// redisDAO.clearRedisTemplate();
				// // 绑定新id
				// int uidredisIndex = CalendarUtil.getRedisIndex(uid);
				// String uidredisTemplate = "redisTemplate" + uidredisIndex;
				// redisDAO.setRedisTemplate(uidredisTemplate);
				// int newValueExist = redisDAO.sSetValueExist("device" + uid,
				// devicetoken);
				// if (newValueExist == 0) {
				// int dbIndex = CalendarUtil.getDbIndex(uid);
				// String dataSource = "dataSource" + dbIndex;
				// DBContextHolder.setDBType(dataSource);
				// DeviceInfo deviceInfo = userDAO.getDeviceInfo(uid,
				// devicetoken);
				//
				// if (deviceInfo != null && deviceInfo.getStatus() == 0) {
				// // 修改
				// userDAO.updateDevicetokenStatus(uid, devicetoken, 1);
				// }
				// if (deviceInfo == null) {
				// // 为空
				// userDAO.saveDevicetoken(uid, devicetoken, 1);
				// }
				// DBContextHolder.clearDBType();
				// // 写入缓存中
				// redisDAO.sAddSetValue("device" + uid, devicetoken);
				// }
				// redisDAO.clearRedisTemplate();
				// }

			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}

}
