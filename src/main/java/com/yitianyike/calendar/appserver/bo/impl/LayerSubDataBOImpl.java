/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.yitianyike.calendar.appserver.bo.LayerSubDataBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

@Component("layerSubDataBO")
public class LayerSubDataBOImpl implements LayerSubDataBO {

	private static Logger logger = Logger.getLogger(LayerSubDataBOImpl.class.getName());

	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String aids = map.get("aids");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationUidAndToken(token)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}
			if (!ParameterValidation.validationAidsAndAidAndType(aids)) {
				appResponse.setRespContent("[]");
				break;
			}

			int redisIndex = CalendarUtil.getRedisIndex(token);
			String redisTemplate = "redisTemplate" + redisIndex;
			redisDAO.setRedisTemplate(redisTemplate);

			Map<String, String> tokenMap = redisDAO.hGetAll(token);

			String uid = tokenMap.get("uid");
			if (uid != null) {

				Map<String, String> uidMap = redisDAO.hGetAll(uid);
				String columnIds = uidMap.get("list");

				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put("sub_status_list", columnIds);

				String subscribeKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-layersub";
				String layersub = redisDAO.hGetValue(subscribeKey, aids);

				if (layersub == null) {
					if (redisDAO.keyExist(subscribeKey) == 0) {// redis中对应的key不存在
						List<DataInfo> dataInfos = dataDAO.getDataInfosBykeyAndField(subscribeKey, aids);
						if (dataInfos == null || dataInfos.size() == 0) {
							appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
							logger.info(requestIndex + " : all subscribe list no exist in data cache, return 500");
						} else {
							String cacheValue = dataInfos.get(0).getCacheValue();
							redisDAO.hSetValue(subscribeKey, aids, cacheValue);
							List<Object> data_list = new ArrayList<Object>();
							data_list.add(JSONObject.parse(dataInfos.get(0).getCacheValue()));
							responseMap.put("data_list", data_list);

							appResponse.setRespContent(JSONObject.toJSONString(responseMap));

						}
					} else {
						appResponse.setCode(EnumConstants.CALENDAR_ERROR_500);
						logger.info(requestIndex + " : all subscribe list redis key exist, but no data, return 500");
					}
				} else {
					List<Object> data_list = new ArrayList<Object>();
					data_list.add(JSONObject.parse(layersub));
					responseMap.put("data_list", data_list);
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
