/**
 * 
 */
package com.yitianyike.calendar.appserver.bo.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.MergeSubscriptionsBO;
import com.yitianyike.calendar.appserver.bo.UnbindBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.RegisterInfo;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DBContextHolder;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.ParameterValidation;

/**
 * @author xujinbo
 *
 */
@Component("unbindBO")
public class UnbindBOImpl implements UnbindBO {

	private static Logger logger = Logger.getLogger(UnbindBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private RegisterDAO registerDAO = (RegisterDAO) DataAccessFactory.dataHolder().get("registerDAO");
	private RedisDAO redisDAO = (RedisDAO) DataAccessFactory.dataHolder().get("redisDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String mobile = map.get("telephone");
		String thirdId = map.get("thirdid");
		String uuid = map.get("uuid");
		String status = map.get("status");
		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		RegisterInfo registerInfoFromUUID = null;
		RegisterInfo registerInfoFromThirdId = null;
		Map<String, String> registerMap = new HashMap<String, String>();

		do {
			if (ParameterValidation.validationMobile(mobile)) {/// 通过手机号码注册登录
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_501);
				logger.info(requestIndex + " : user unbind by mobile, no support, return 501");

			}

			// 解除绑定,stats0为不保存数据到uuid,并清除uuid下所有数据;stats1为保存数据到uuid
			if (ParameterValidation.validationThirdIdOrUuid(uuid)
					&& ParameterValidation.validationThirdIdOrUuid(thirdId)
					&& ParameterValidation.validationStatus(status)) {
				registerInfoFromUUID = registerDAO.getRegisterInfo(uuid, "uuid");

				if (registerInfoFromUUID != null) {
					String uidFromUUID = registerInfoFromUUID.getUid();
					if (StringUtils.isNotBlank(uidFromUUID)) {
						int dbIndex = CalendarUtil.getDbIndex(uidFromUUID);
						String dataSource = "dataSource" + dbIndex;
						DBContextHolder.setDBType(dataSource);
						userDAO.delSubscribeList(uidFromUUID);
						DBContextHolder.clearDBType();
						// 保存三方订阅到uuid数据
						if (status.equals("1")) {
							registerInfoFromThirdId = registerDAO.getRegisterInfo(thirdId, "third_id");
							if (registerInfoFromThirdId != null) {
								int dbIndex1 = CalendarUtil.getDbIndex(registerInfoFromThirdId.getUid());
								String dataSource1 = "dataSource" + dbIndex1;
								DBContextHolder.setDBType(dataSource1);
								List<Map<String, String>> subscribedList = userDAO
										.getSubscribedList(registerInfoFromThirdId.getUid());
								DBContextHolder.clearDBType();
								if (!subscribedList.isEmpty()) {
									Set<Map<String, String>> set = new HashSet<Map<String, String>>();
									set.addAll(subscribedList);
									userDAO.saveSubscribe(uidFromUUID, set);
								}
							}
						}
						// 删除redis中的uid的数据
						int redisIndex = CalendarUtil.getRedisIndex(uidFromUUID);
						String redisTemplate = "redisTemplate" + redisIndex;
						redisDAO.setRedisTemplate(redisTemplate);
						redisDAO.delKey(uidFromUUID);
						redisDAO.clearRedisTemplate();
						registerMap.put("uid", uidFromUUID);
						registerMap.put("status", EnumConstants.CALENDAR_UNBIND_OK);
						appResponse.setRespContent(JSON.toJSONString(registerMap));
						logger.info(requestIndex + " : user unbind by uuid =" + uuid + " and thirdId=" + thirdId
								+ ", return uid = " + uidFromUUID);
					}
				}
			} else {/// 错误
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}
}
