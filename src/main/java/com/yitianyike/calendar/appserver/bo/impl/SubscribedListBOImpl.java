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

import com.yitianyike.calendar.appserver.bo.SubscribedListBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("subscribedListBO")
public class SubscribedListBOImpl implements SubscribedListBO {

	private static Logger logger = Logger.getLogger(SubscribedListBOImpl.class.getName());

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
			List<String> list = null;
			if (uid != null) {

				Map<String, String> uidMap = redisDAO.hGetAll(uid);
//				if (uidMap == null || uidMap.isEmpty() || !uidMap.get("token").equalsIgnoreCase(token)) {
//					appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
//					if (uidMap.get("token") == null) {
//						logger.info(requestIndex + " : token exist, but current token : " + token
//								+ " is expired, return 401");
//					} else {
//						logger.info(requestIndex + " : token exist, but current token : " + token
//								+ " is invalid, new token : " + uidMap.get("token") + ", return 401");
//					}
//					redisDAO.delKey(token);
//					redisDAO.clearRedisTemplate();
//					break;
//				}

				String columnIds = uidMap.get("list");

				if (columnIds != null && columnIds.length() > 0) {
					String[] arr = uidMap.get("list").split(",");
					list = java.util.Arrays.asList(arr);
					String subscribeKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + "subscribed";

					String returnColumnString = "";
					List<String> columnList = redisDAO.hMgetValue(subscribeKey, list);
					columnList.remove(null);
					if (columnList == null || columnList.size() == 0) {
						if (redisDAO.keyExist(subscribeKey) == 0) {// redis中对应的key不存在
							List<DataInfo> dataInfos = dataDAO.getDataInfos(subscribeKey);
							if (dataInfos == null || dataInfos.size() == 0) {
								appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
								logger.info(requestIndex + " : subscribed list no exist in data cache, return 500");
							} else {
								Map<String, String> dataMap = new HashMap<String, String>();

								returnColumnString = "[";
								for (DataInfo info : dataInfos) {
									dataMap.put(info.getField(), info.getCacheValue());
									if (list.contains(info.getField())) {
										returnColumnString += info.getCacheValue() + ",";
									}
								}
								if (!returnColumnString.equals("[")) {
									returnColumnString = returnColumnString.substring(0,
											returnColumnString.length() - 1);
								}

								returnColumnString += "]";
								redisDAO.hMsetValue(subscribeKey, dataMap, "0");
								appResponse.setRespContent(returnColumnString);
							}
						} else {
//							appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
							appResponse.setRespContent("[]");
							logger.info(requestIndex + " : subscribed list redis key exist, but no data");
						}
					} else {
						returnColumnString = "[";
						for (String columnString : columnList) {
							if (columnString != null) {
								returnColumnString += columnString + ",";
							}
						}
						if (!returnColumnString.equals("[")) {
							returnColumnString = returnColumnString.substring(0, returnColumnString.length() - 1);
						}
						returnColumnString += "]";

						appResponse.setRespContent(returnColumnString);
					}
				} else {
					appResponse.setRespContent("[]");
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
