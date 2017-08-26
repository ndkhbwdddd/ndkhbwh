/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.yitianyike.calendar.appserver.bo.UpdateDataBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.DateUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("updateDataBO")
public class UpdateDataBOImpl implements UpdateDataBO {

	private static Logger logger = Logger.getLogger(UpdateDataBOImpl.class.getName());


	private DataDAO dataDAO = (DataDAO) DataAccessFactory.dataHolder().get("dataDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String aids = map.get("aids");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (!ParameterValidation.validationAidsAndAidAndType(aids)
					|| !ParameterValidation.validationUidAndToken(token)) {
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

				String[] arr = aids.split(" ");
				List<String> list = java.util.Arrays.asList(arr);
				String currentDate = DateUtil.getCurrentDate();
				for (String aid : list) {
					String aidKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + aid;

					String tempString = redisDAO.hGetValue(aidKey, currentDate);
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
									if (currentDate.equalsIgnoreCase(info.getField())) {
										tempString = info.getCacheValue();
										if (tempString.charAt(0) == '['
												&& tempString.charAt(tempString.length() - 1) == ']') {
											tempString = tempString.substring(1, tempString.length() - 1);
										}
										valueString += tempString + ",";
									}
								}
								redisDAO.hMsetAndZadd(aidKey, dataMap);
							} else {
								logger.info(
										requestIndex + " : aid key = " + aidKey + " card data no exist in data cache");
							}
						} else {
							logger.info(requestIndex + " : aid key = " + aidKey + " redis key exist, but no data");
						}
					}
				}
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

	@Override
	public AppResponse updateByDate(Map<String, String> map, String content, long requestIndex) {
		String token = map.get("token");
		String aids = map.get("aids");
		String minTime = map.get("mintime");
		String maxTime = map.get("maxtime");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		do {
			if (token == null || aids == null || maxTime == null || token.length() < 8 || aids.length() == 0) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");
				break;
			}
			String check = aids.replaceAll(" ", "") + maxTime;
			boolean result = check.matches("[0-9]+");
			if (result == false) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error 2, return 400");
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
				if (uidMap == null || uidMap.isEmpty() || !uidMap.get("token").equalsIgnoreCase(token)) {
					appResponse.setCode(EnumConstants.CALENDAR_ERROR_401);
					if (uidMap.get("token") == null) {
						logger.info(requestIndex + " : token exist, but current token : " + token
								+ " is expired, return 401");
					} else {
						logger.info(requestIndex + " : token exist, but current token : " + token
								+ " is invalid, new token : " + uidMap.get("token") + ", return 401");
					}
					redisDAO.delKey(token);
					redisDAO.clearRedisTemplate();
					break;
				}

				String[] arr = aids.split(" ");
				List<String> list = java.util.Arrays.asList(arr);
				// String currentDate = DateUtil.getCurrentDate();
				for (String aid : list) {
					String aidKey = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + aid;
					String aidKeySet = uidMap.get("channel") + "-" + uidMap.get("version") + "-" + aid + "-zset";

					if ("".equalsIgnoreCase(minTime) || minTime == null) {
						String tempString = redisDAO.hGetValue(aidKey, maxTime);
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
										if (maxTime.compareToIgnoreCase(info.getField()) <= 0
												&& maxTime.compareToIgnoreCase(info.getField()) >= 0) {
											tempString = info.getCacheValue();
											if (tempString.charAt(0) == '['
													&& tempString.charAt(tempString.length() - 1) == ']') {
												tempString = tempString.substring(1, tempString.length() - 1);
											}
											valueString += tempString + ",";
										}
									}
									redisDAO.hMsetAndZadd(aidKey, dataMap);
								} else {
									logger.info(requestIndex + " : aid key = " + aidKey
											+ " card data no exist in data cache");
								}
							} else {
								logger.info(requestIndex + " : aid key = " + aidKey + " redis key exist, but no data");
							}
						}
					} else {
						List<String> fieldList = redisDAO.zRangeByScoreWithScores(aidKeySet, Integer.parseInt(minTime),
								Integer.parseInt(maxTime));

						if (fieldList != null && fieldList.size() > 0) {
							List<String> valueList = redisDAO.hMgetValue(aidKey, fieldList);
							if (valueList != null && valueList.size() > 0) {
								for (String tempString : valueList) {
									if (tempString.charAt(0) == '['
											&& tempString.charAt(tempString.length() - 1) == ']') {
										tempString = tempString.substring(1, tempString.length() - 1);
									}
									valueString += tempString + ",";
								}
							}
						} else {
							if (redisDAO.keyExist(aidKeySet) == 0) {
								logger.info(requestIndex + " zset key = " + aidKeySet + ", no exists!");
								List<DataInfo> dataInfos = dataDAO.getDataInfos(aidKey);
								if (dataInfos != null && dataInfos.size() > 0) {
									Map<String, String> dataMap = new HashMap<String, String>();
									for (DataInfo info : dataInfos) {
										dataMap.put(info.getField(), info.getCacheValue());
										if (maxTime.compareToIgnoreCase(info.getField()) <= 0
												&& maxTime.compareToIgnoreCase(info.getField()) >= 0) {
											String tempString = info.getCacheValue();
											if (tempString.charAt(0) == '['
													&& tempString.charAt(tempString.length() - 1) == ']') {
												tempString = tempString.substring(1, tempString.length() - 1);
											}
											valueString += tempString + ",";
										}
									}
									redisDAO.hMsetAndZadd(aidKey, dataMap);
								} else {
									logger.info(requestIndex + " : aid key = " + aidKey
											+ " card data no exist in data cache");
								}
							} else {
								logger.info(requestIndex + " : aid key set = " + aidKeySet
										+ " redis key exist, but no data");
							}
						}
					}
				}
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

}
