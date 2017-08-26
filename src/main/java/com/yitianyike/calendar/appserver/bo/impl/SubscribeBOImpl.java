/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yitianyike.calendar.appserver.bo.SubscribeBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;
import com.yitianyike.calendar.appserver.util.PushRet;
import com.yitianyike.calendar.appserver.util.PushUtil;

/**
 * @author xujinbo
 *
 */
@Component("subscribeBO")
public class SubscribeBOImpl implements SubscribeBO {

	private static Logger logger = Logger.getLogger(SubscribeBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		// String type = map.get("type");
		String columnId = map.get("aid");
		// String devicetoken = map.get("devicetoken");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			// if (!ParameterValidation.validationUidAndToken(token)
			// || !ParameterValidation.validationAidsAndAidAndType(type)
			// || !ParameterValidation.validationAidsAndAidAndType(columnId)) {
			// appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
			// logger.error(requestIndex + " : param error, return 400");
			// break;
			// }

			if (!ParameterValidation.validationUidAndToken(token)
					|| !ParameterValidation.validationAidsAndAidAndType(columnId)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			int redisIndex = CalendarUtil.getRedisIndex(token);
			String redisTemplate = "redisTemplate" + redisIndex;
			redisDAO.setRedisTemplate(redisTemplate);

			Map<String, String> tokenMap = redisDAO.hGetAll(token);

			int exists = 0;
			String uid = tokenMap.get("uid");
			if (uid != null) {

				Map<String, String> uidMap = redisDAO.hGetAll(uid);

				// =============================单点登录==================
				// if (uidMap == null || uidMap.isEmpty() ||
				// !uidMap.get("token").equalsIgnoreCase(token)) {
				// appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				// if (uidMap.get("token") == null) {
				// logger.info(requestIndex + " : token exist, but current token
				// : " + token
				// + " is expired, return 401");
				// } else {
				// logger.info(requestIndex + " : token exist, but current token
				// : " + token
				// + " is invalid, new token : " + uidMap.get("token") + ",
				// return 401");
				// }
				// redisDAO.delKey(token);
				// redisDAO.clearRedisTemplate();
				// break;
				// }

				String columnIds = uidMap.get("list");
				List<String> list = null;
				if (columnIds != null && columnIds.length() > 0) {
					String[] arr = uidMap.get("list").split(",");
					list = java.util.Arrays.asList(arr);

					for (String id : list) {
						if (columnId.equalsIgnoreCase(id)) {
							exists = 1;
							break;
						}
					}
				}

				if (exists == 0) {// 用户未订阅此订阅项
					List<String> columnList = null;
					userDAO.saveSubscribeId(uid, columnId, 0);
					if (list == null) {
						columnList = new ArrayList<String>();
					} else {
						columnList = new ArrayList<String>(list);
					}
					columnList.add(columnId);
					DBContextHolder.clearDBType();
					redisDAO.hsetColumnList(uid, columnList);
				}
				// if (exists == 0) {/// 用户未订阅该项
				// /// 若type为星座类型，之前订阅的星座需要先删除
				//
				// List<String> columnList = null;
				//
				// int dbIndex = CalendarUtil.getDbIndex(uid);
				// String dataSource = "dataSource" + dbIndex;
				// DBContextHolder.setDBType(dataSource);
				// if (Integer.parseInt(type) == 100) {// 假如星座类型为100
				// userDAO.delSubscribeIdByType(uid, Integer.parseInt(type));
				// userDAO.saveSubscribeId(uid, columnId,
				// Integer.parseInt(type));
				// columnList = userDAO.getSubscribeList(uid);
				// } else {
				// userDAO.saveSubscribeId(uid, columnId,
				// Integer.parseInt(type));
				// if (list == null) {
				// columnList = new ArrayList<String>();
				// } else {
				// columnList = new ArrayList<String>(list);
				// }
				// columnList.add(columnId);
				// }
				// DBContextHolder.clearDBType();
				//
				// redisDAO.hsetColumnList(uid, columnList);
				//
				// } else {/// 用户已订阅该项

				// }

				/// 返回该订阅项数据
				String aidKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + "downzip";

				// String downzipUrl = redisDAO.getValue(aidKey);
				String downzipUrl = redisDAO.hGetValue(aidKey, columnId);
				Map<String, Object> responseMap = new HashMap<String, Object>();
				if (downzipUrl == null) {
					responseMap.put("downzip_status", 0);
					responseMap.put("downzip_url", "");
				} else {
					responseMap.put("downzip_status", 1);
					responseMap.put("downzip_url", downzipUrl);
				}
				//
				// Map<String, String> valueMap = redisDAO.hGetAll(aidKey);
				//
				// if (valueMap != null && valueMap.size() > 0) {
				// for (Entry<String, String> entry : valueMap.entrySet()) {
				// String tempString = entry.getValue();
				// if (tempString.charAt(0) == '[' &&
				// tempString.charAt(tempString.length() - 1) == ']') {
				// tempString = tempString.substring(1, tempString.length() -
				// 1);
				// }
				// valueString += tempString + ",";
				// }
				// } else {
				// if (redisDAO.keyExist(aidKey) == 0) {
				// logger.info(requestIndex + " : hash key = " + aidKey + ", no
				// exists!");
				// List<DataInfo> dataInfos = dataDAO.getDataInfos(aidKey);
				// if (dataInfos != null && dataInfos.size() > 0) {
				// Map<String, String> dataMap = new HashMap<String, String>();
				// for (DataInfo info : dataInfos) {
				// dataMap.put(info.getField(), info.getCacheValue());
				// String tempString = info.getCacheValue();
				// if (tempString.charAt(0) == '[' &&
				// tempString.charAt(tempString.length() - 1) == ']') {
				// tempString = tempString.substring(1, tempString.length() -
				// 1);
				// }
				// valueString += tempString + ",";
				// }
				// redisDAO.hMsetAndZadd(aidKey, dataMap);
				// } else {
				// logger.info(requestIndex + " : aid key = " + aidKey + " card
				// data no exist in data cache");
				// }
				// } else {
				// logger.info(requestIndex + " : aid key = " + aidKey + " redis
				// key exist, but no data");
				// }
				// }
				//
				// if (valueString.length() > 0) {
				// valueString = "[" + valueString.substring(0,
				// valueString.length() - 1) + "]";
				// } else {
				// valueString = "[]";
				// }
				appResponse.setRespContent(JSONObject.toJSONString(responseMap));

				// =================================================================处理推送

				// if (redisDAO.sSetCard("device" + uid) > 1) {
				// Map<String, Object> pushmap = new HashMap<String, Object>();
				// pushmap.put("uid", uid);
				// pushmap.put("status", 1);
				// pushmap.put("aid", columnId);
				// Set<String> sGetAll = redisDAO.sGetAll("device" + uid);
				// if (sGetAll != null && sGetAll.size() > 1) {
				// if (devicetoken != null) {
				// Iterator<String> iterator = sGetAll.iterator();
				// while (iterator.hasNext()) {
				// if (iterator.next().equals(devicetoken)) {
				// iterator.remove();
				// }
				// }
				// }
				// String[] pushTokens = sGetAll.toArray(new String[] {});
				// PushRet batchSend = PushUtil.batchSend(pushTokens,
				// JSON.toJSONString(pushmap));
				// String message = batchSend.getMessage();
				// System.out.println(message);
				// logger.info("=========订阅推送返回信息=================================="
				// + message);
				//
				// }
				// }

			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");
			}

			redisDAO.clearRedisTemplate();

		} while (false);

		return appResponse;
	}

}
