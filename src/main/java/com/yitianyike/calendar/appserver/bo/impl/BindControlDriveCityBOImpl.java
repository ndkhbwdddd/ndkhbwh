/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.yitianyike.calendar.appserver.bo.BindControlDriveCityBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

@Component("bindControlDriveCityBO")
public class BindControlDriveCityBOImpl implements BindControlDriveCityBO {

	private static Logger logger = Logger.getLogger(BindControlDriveCityBOImpl.class.getName());

	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String aids = map.get("aids");
		String dateTime = map.get("datetime");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationUidAndToken(token)
					//|| !ParameterValidation.validationAidsAndAidAndType(aids)
					|| !ParameterValidation.validationDateTime(dateTime)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			int redisIndex = CalendarUtil.getRedisIndex(token);
			String redisTemplate = "redisTemplate" + redisIndex;
			redisDAO.setRedisTemplate(redisTemplate);

			Map<String, String> tokenMap = redisDAO.hGetAll(token);

			String uid = tokenMap.get("uid");
			String valueString = "";
			if (uid != null) {
				Map<String, String> uidMap = redisDAO.hGetAll(uid);
				String usercity = uidMap.get("usercity");
				if (aids != usercity) {
					redisDAO.hSetValue(uid, "usercity", aids);
				}
				valueString = getDataByAid(requestIndex, dateTime, valueString, uidMap, aids);

				if (valueString.length() > 0) {
					valueString = "[" + valueString.substring(0, valueString.length() - 1) + "]";
				} else {
					valueString = "[]";
				}
				appResponse.setRespContent(valueString);
			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");
			}
			redisDAO.clearRedisTemplate();

		} while (false);

		return appResponse;

	}

	private String getDataByAid(long requestIndex, String dateTime, String valueString, Map<String, String> uidMap,
			String aid) {
		String aidKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + aid;
		String tempString = redisDAO.hGetValue(aidKey, dateTime);
		if (tempString != null) {
			if (tempString.charAt(0) == '[' && tempString.charAt(tempString.length() - 1) == ']') {
				tempString = tempString.substring(1, tempString.length() - 1);
			}
			valueString += tempString + ",";
		} else {
			if (redisDAO.keyExist(aidKey) == 0) {
				logger.info(requestIndex + " : hash key = " + aidKey + ", no exists!");
				List<DataInfo> dataInfos = dataDAO.getDataInfos(aidKey);
				if (dataInfos != null && dataInfos.size() > 0) {
					Map<String, String> dataMap = new HashMap<String, String>();
					for (DataInfo info : dataInfos) {
						dataMap.put(info.getField(), info.getCacheValue());
						if (dateTime.compareToIgnoreCase(info.getField()) == 0) {
							tempString = info.getCacheValue();
							if (tempString.charAt(0) == '[' && tempString.charAt(tempString.length() - 1) == ']') {
								tempString = tempString.substring(1, tempString.length() - 1);
							}
							valueString += tempString + ",";
						}
					}
					redisDAO.hMsetAndZadd(aidKey, dataMap);
				} else {
					logger.info(requestIndex + " : aid key = " + aidKey + " card data no exist in data cache");
				}
			} else {
				logger.info(requestIndex + " : aid key = " + aidKey + " redis key exist, but no data");
			}
		}
		return valueString;
	}

}
