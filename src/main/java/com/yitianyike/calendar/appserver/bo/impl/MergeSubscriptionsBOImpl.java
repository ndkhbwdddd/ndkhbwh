/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.MergeSubscriptionsBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.RegisterInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("mergeSubscriptionsBO")
public class MergeSubscriptionsBOImpl implements MergeSubscriptionsBO {

	private static Logger logger = Logger.getLogger(MergeSubscriptionsBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private RegisterDAO registerDAO = (RegisterDAO) DataAccessFactory.dataHolder().get("registerDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String mobile = map.get("telephone");
		String thirdId = map.get("thirdid");
		String uuid = map.get("uuid");
		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		RegisterInfo registerInfoFromUUID = null;
		RegisterInfo registerInfoFromThirdId = null;
		Map<String, String> registerMap = new HashMap<String, String>();

		do {
			if (ParameterValidation.validationMobile(mobile)) {/// 通过手机号码注册登录
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_501);
				logger.info(requestIndex + " : user register by mobile, no support, return 501");
				break;
			}

			// 第三方id关联到uuid
			if (ParameterValidation.validationThirdIdOrUuid(uuid)
					&& ParameterValidation.validationThirdIdOrUuid(thirdId)) {
				// 查询uuid是否注册,否返回错误码
				registerInfoFromUUID = registerDAO.getRegisterInfo(uuid, "uuid");
				if (registerInfoFromUUID == null) {
					registerMap.put("uid", "");
					registerMap.put("status", EnumConstants.CALENDAR_REGISTER_NO);
					appResponse.setRespContent(JSON.toJSONString(registerMap));
					logger.info(requestIndex + " : user have not registered  by uuid");
					break;
				}
				// 查询三方id是否注册,否进行注册并关联uuid
				registerInfoFromThirdId = registerDAO.getRegisterInfo(thirdId, "third_id");
				if (registerInfoFromThirdId == null) {
					int registerIndex = CalendarUtil.getRegisterTableIndex(thirdId);
					int uuidIndex = CalendarUtil.getRegisterTableIndex(uuid);
					// 不在一个库多生成一份
					if (registerIndex != uuidIndex) {
						registerInfoFromUUID.setThirdId(thirdId);
						registerDAO.saveRegisterInfo(registerInfoFromUUID);
					} else {
						// 在一个库就修改
						registerDAO.updateThirdIdByUuid(thirdId, uuid);
					}
					// 三方id第一次注册,就不用同步数据,直接返回uid
					registerMap.put("uid", registerInfoFromUUID.getUid());
					registerMap.put("status", EnumConstants.CALENDAR_MEGER_OK);
					appResponse.setRespContent(JSON.toJSONString(registerMap));
					logger.info(requestIndex + " : user first register merge by uuid =" + uuid + " and thirdId="
							+ thirdId + ", return uid = " + registerInfoFromUUID.getUid());

				} else {
					// 是,两者进行关联,查看原uid订阅项,同步到新的uid订阅项中
					List<Map<String, String>> subscribeListFromUUID = userDAO
							.getSubscribedList(registerInfoFromUUID.getUid());
					List<Map<String, String>> subscribeListFromThirdId = userDAO
							.getSubscribedList(registerInfoFromThirdId.getUid());
					if (!subscribeListFromUUID.isEmpty() && !subscribeListFromUUID.equals(subscribeListFromThirdId)) {
						userDAO.delSubscribeList(registerInfoFromThirdId.getUid());
						// 合并订阅项,处理单项订阅
						Set<Map<String, String>> set = new HashSet<Map<String, String>>(subscribeListFromThirdId);
						set.addAll(subscribeListFromUUID);

						// 假设星座的type为100
						int i = 0;
						for (Map<String, String> map2 : subscribeListFromThirdId) {
							if (map2.get("type").equals("100")) {
								i = 1;
							}
						}
						if (i == 1) {
							for (Map<String, String> map2 : subscribeListFromUUID) {
								if (map2.get("type").equals("100")) {
									set.remove(map2);
								}
							}
						}
						// 添加到db中
						userDAO.saveSubscribe(registerInfoFromThirdId.getUid(), set);
						// 删除redis中的uid的数据
						int redisIndex = CalendarUtil.getRedisIndex(registerInfoFromThirdId.getUid());
						String redisTemplate = "redisTemplate" + redisIndex;
						redisDAO.setRedisTemplate(redisTemplate);
						redisDAO.delKey(registerInfoFromThirdId.getUid());
						redisDAO.clearRedisTemplate();
					}

					registerMap.put("uid", registerInfoFromThirdId.getUid());
					registerMap.put("status", EnumConstants.CALENDAR_MEGER_OK);
					appResponse.setRespContent(JSON.toJSONString(registerMap));
					logger.info(requestIndex + " : user first register merge by uuid =" + uuid + " and thirdId="
							+ thirdId + ", return uid = " + registerInfoFromThirdId.getUid());
				}

			} else {/// 错误
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}
}
