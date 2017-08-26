/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yitianyike.calendar.appserver.bo.RecommendSubscribeListBO;
import com.yitianyike.calendar.appserver.bo.TabsListBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;


@Component("tabsListBO")
public class TabsListBOImpl implements TabsListBO {

	private static Logger logger = Logger.getLogger(TabsListBOImpl.class.getName());

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

				Map<String, String> uidMap = redisDAO.hGetAll(uid);

				String subscribeKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + "tabs";

				String allSubscribeString = redisDAO.hGetValue(subscribeKey, subscribeKey);
				if (allSubscribeString == null) {
					if (redisDAO.keyExist(subscribeKey) == 0) {// redis中对应的key不存在
						List<DataInfo> dataInfos = dataDAO.getDataInfos(subscribeKey);
						if (dataInfos == null || dataInfos.size() == 0) {
							appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
							logger.info(
									requestIndex + " : tabs list no exist in data cache, return 500");
						} else {
							redisDAO.hSetValue(subscribeKey, subscribeKey, dataInfos.get(0).getCacheValue());
							appResponse.setRespContent(dataInfos.get(0).getCacheValue());
						}
					} else {
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
						logger.info(
								requestIndex + " : tabs list redis key exist, but no data, return 500");
					}
				} else {
					appResponse.setRespContent(allSubscribeString);
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
