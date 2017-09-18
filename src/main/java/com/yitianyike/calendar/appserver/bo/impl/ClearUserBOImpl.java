/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.ClearUserBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.AuthAccount;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.RegisterInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;
import com.yitianyike.calendar.appserver.util.PropertiesUtil;

@Component("clearUserBO")
public class ClearUserBOImpl implements ClearUserBO {

	private static Logger logger = Logger.getLogger(ClearUserBOImpl.class.getName());

	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");
	@Autowired
	private UserDAO userDAO;

	private RegisterDAO registerDAO = (RegisterDAO) DataAccessFactory.dataHolder().get("registerDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {

		String uuid = map.get("uuid");
		String uid = map.get("uid");
		String token = map.get("token");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationUidAndToken(token) || !ParameterValidation.validationUidAndToken(uid)
					|| !ParameterValidation.validationThirdIdOrUuid(uuid)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}

			// 删除注册信息
			registerDAO.deleteRegisterInfoByUUID(uuid);

			int dbIndex = CalendarUtil.getDbIndex(uid);
			String dataSource = "dataSource" + dbIndex;
			DBContextHolder.setDBType(dataSource);
			// 删除注册用户信息
			userDAO.deleteAuthAccountByUid(uid);

			// 清除用户订阅
			userDAO.deleteUserSubscribeId(uid);
			DBContextHolder.clearDBType();

			// 删除缓存中的uid和token
			int redisIndex = CalendarUtil.getRedisIndex(uid);
			String redisTemplate = "redisTemplate" + redisIndex;
			redisDAO.setRedisTemplate(redisTemplate);
			redisDAO.delKey(uid);
			redisDAO.delKey(token);
			redisDAO.clearRedisTemplate();
			Map<String, String> clearUserMap = new HashMap<String, String>();
			clearUserMap.put("code", 1 + "");
			clearUserMap.put("msg", "success");
			appResponse.setRespContent(JSON.toJSONString(clearUserMap));
			logger.info(requestIndex + "uid:" + uid + "," + "uuid:" + uuid + "," + "token:" + token + ","
					+ " : user clear successfully, return token = " + token);
		} while (false);

		return appResponse;
	}

}
