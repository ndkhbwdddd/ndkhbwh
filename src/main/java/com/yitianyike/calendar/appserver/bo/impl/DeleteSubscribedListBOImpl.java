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
import com.yitianyike.calendar.appserver.bo.DeleteSubscribedListBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("deleteSubscribedListBO")
public class DeleteSubscribedListBOImpl implements DeleteSubscribedListBO {

	private static Logger logger = Logger.getLogger(SubscribedListBOImpl.class.getName());
	@Autowired
	private UserDAO userDAO;
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

//				Map<String, String> uidMap = redisDAO.hGetAll(uid);
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

				String columnString = "";
				redisDAO.hSetValue(uid, "list", columnString);
				int dbIndex = CalendarUtil.getDbIndex(uid);
				String dataSource = "dataSource" + dbIndex;
				DBContextHolder.setDBType(dataSource);
				userDAO.delSubscribeList(uid);
				DBContextHolder.clearDBType();
				Map<String, String> rmap = new HashMap<String, String>();
				rmap.put("uid", uid);
				rmap.put("status", EnumConstants.CALENDAR_DEKETE_SUB_OK);
				appResponse.setRespContent(JSON.toJSONString(rmap));
			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");
			}
			redisDAO.clearRedisTemplate();

		} while (false);

		return appResponse;
	}

}
