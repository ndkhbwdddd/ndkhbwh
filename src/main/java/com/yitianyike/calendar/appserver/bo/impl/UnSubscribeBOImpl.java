/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.UnSubscribeBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
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
@Component("unSubscribeBO")
public class UnSubscribeBOImpl implements UnSubscribeBO {

	private static Logger logger = Logger.getLogger(UnSubscribeBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		// String type = map.get("type");
		String columnId = map.get("aid");
		// String devicetoken = map.get("devicetoken");
		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		Map<String, Object> unSubscribeMap = new HashMap<String, Object>();

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
			int columnIndex = 0;
			String uid = tokenMap.get("uid");
			if (uid != null) {

				Map<String, String> uidMap = redisDAO.hGetAll(uid);
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

				String[] arr = uidMap.get("list").split(",");
				List<String> list = java.util.Arrays.asList(arr);

				for (String id : list) {
					if (columnId.equalsIgnoreCase(id)) {
						exists = 1;
						break;
					}
					columnIndex++;
				}

				if (exists == 0) {/// 用户未订阅该项
					unSubscribeMap.put("status", 1);
					unSubscribeMap.put("message", EnumConstants.CALENDAR_UNSUBSCRIBE_NOEXISTS);
				} else {/// 用户已订阅该项
					int dbIndex = CalendarUtil.getDbIndex(uid);
					String dataSource = "dataSource" + dbIndex;
					DBContextHolder.setDBType(dataSource);
					// userDAO.delSubscribeId(uid, columnId);
					int type = 0;
					userDAO.updateSubscribeId(uid, columnId, type);
					DBContextHolder.clearDBType();

					List<String> columnList = new ArrayList<String>(list);
					columnList.remove(columnIndex);
					redisDAO.hsetColumnList(uid, columnList);

					unSubscribeMap.put("status", 1);
					unSubscribeMap.put("message", EnumConstants.CALENDAR_UNSUBSCRIBE_OK);
				}

				appResponse.setRespContent(JSON.toJSONString(unSubscribeMap));

				// 处理推送
				// if (redisDAO.sSetCard("device" + uid) > 1) {
				// Map<String, Object> pushmap = new HashMap<String, Object>();
				// pushmap.put("uid", uid);
				// pushmap.put("status", 0);
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
				// logger.info("=========取消订阅推送返回信息=================================="
				// + message);
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
