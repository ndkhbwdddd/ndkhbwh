/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yitianyike.calendar.appserver.bo.AllSubscribeListBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("allSubscribeListBO")
public class AllSubscribeListBOImpl implements AllSubscribeListBO {

	private static Logger logger = Logger.getLogger(AllSubscribeListBOImpl.class.getName());

	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationUidAndToken(token)) {
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

				// String checkToken = redisDAO.hGetValue(uid, "token");
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

				String columnIds = uidMap.get("list");

				String subscribeKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + "complete";

				String allSubscribeString = redisDAO.hGetValue(subscribeKey, subscribeKey);
				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put("sub_status_list", columnIds);

				if (allSubscribeString == null) {
					if (redisDAO.keyExist(subscribeKey) == 0) {// redis中对应的key不存在
						List<DataInfo> dataInfos = dataDAO.getDataInfos(subscribeKey);
						if (dataInfos == null || dataInfos.size() == 0) {
							appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
							logger.info(requestIndex + " : all subscribe list no exist in data cache, return 500");
						} else {
							redisDAO.hSetValue(subscribeKey, subscribeKey, dataInfos.get(0).getCacheValue());

							responseMap.put("data_list", JSONArray.parse(dataInfos.get(0).getCacheValue()));

							appResponse.setRespContent(JSONObject.toJSONString(responseMap));
						}
					} else {
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
						logger.info(requestIndex + " : all subscribe list redis key exist, but no data, return 500");
					}
				} else {
					responseMap.put("data_list", JSONArray.parse(allSubscribeString));
					appResponse.setRespContent(JSONObject.toJSONString(responseMap));

				}
			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");
			}
			redisDAO.clearRedisTemplate();
		} while (false);

		return appResponse;
	}

}
