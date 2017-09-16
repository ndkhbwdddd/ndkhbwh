/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.yitianyike.calendar.appserver.bo.ChannelCodesBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.DateUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

@Component("channelCodesBO")
public class ChannelCodesBOImpl implements ChannelCodesBO {

	private static Logger logger = Logger.getLogger(ChannelCodesBOImpl.class.getName());

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
			String valueString = "";
			if (uid != null) {
				Map<String, String> uidMap = redisDAO.hGetAll(uid);
				String sb = "all-" + uidMap.get("version") + "-channel";
				valueString = redisDAO.hGetValue(sb, sb);
				if (StringUtils.isNotBlank(valueString)) {
					appResponse.setRespContent(valueString);
				} else {
					if (redisDAO.keyExist(sb) == 0) {
						List<DataInfo> dataInfos = dataDAO.getDataInfos(sb);
						if (dataInfos != null && dataInfos.size() > 0) {
							String cacheValue = dataInfos.get(0).getCacheValue();
							redisDAO.hSetValue(sb, sb, cacheValue);
							appResponse.setRespContent(valueString);
						}
					}
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
