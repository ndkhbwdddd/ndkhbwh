/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.UnbindPushBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
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
@Component("unbindPushBO")
public class UnbindPushBOImpl implements UnbindPushBO {

	private static Logger logger = Logger.getLogger(UnbindPushBOImpl.class.getName());
	@Autowired
	private UserDAO userDAO;
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String devicetoken = map.get("devicetoken");

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

				// 未绑定或者需要将这个uid下所有devicetoken全部解绑
				Map<String, String> resultmap = new HashMap<String, String>();
				if (devicetoken == null) {
					// 查看是否绑定
					int devicetokenCount = redisDAO.keyExist("device" + uid);
					if (devicetokenCount == 0) {
						resultmap.put("status", EnumConstants.CALENDAR_NOTBIND_PUSH_FINISHED);
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
						appResponse.setRespContent(JSON.toJSONString(resultmap));
						redisDAO.clearRedisTemplate();
						break;
					} else {// 删除uid下所有的推送
						redisDAO.delKey("device" + uid);
						int dbIndex = CalendarUtil.getDbIndex(uid);
						String dataSource = "dataSource" + dbIndex;
						DBContextHolder.setDBType(dataSource);
						// 解绑
						userDAO.updateDevicetokenStatusByUid(uid, 0);
						DBContextHolder.clearDBType();
					}

				} else {
					int exist = redisDAO.sSetValueExist("device" + uid, devicetoken);
					if (exist == 0) {
						appResponse.setRespContent(JSON.toJSONString(resultmap));
						resultmap.put("status", EnumConstants.CALENDAR_NOTBIND_PUSH_FINISHED);
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
						appResponse.setRespContent(JSON.toJSONString(resultmap));
						redisDAO.clearRedisTemplate();
						break;
					} else {
						redisDAO.delSetValue("device" + uid, devicetoken);
						int dbIndex = CalendarUtil.getDbIndex(uid);
						String dataSource = "dataSource" + dbIndex;
						DBContextHolder.setDBType(dataSource);
						// 解绑
						userDAO.updateDevicetokenStatus(uid, devicetoken, 0);
						DBContextHolder.clearDBType();
					}
				}
				resultmap.put("code", "1");
				resultmap.put("status", EnumConstants.CALENDAR_UNBIND_PUSH_OK);
				appResponse.setRespContent(JSON.toJSONString(resultmap));
			} else {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
				logger.error(requestIndex + " : token no exist, return 401");

			}
			redisDAO.clearRedisTemplate();

		} while (false);

		return appResponse;
	}
}
