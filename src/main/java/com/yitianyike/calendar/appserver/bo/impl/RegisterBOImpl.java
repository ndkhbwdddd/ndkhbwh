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
import com.yitianyike.calendar.appserver.bo.RegisterBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.AuthAccount;
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
@Component("registerBO")
public class RegisterBOImpl implements RegisterBO {

	private static Logger logger = Logger.getLogger(RegisterBOImpl.class.getName());

	@Autowired
	private UserDAO userDAO;

	private RegisterDAO registerDAO = (RegisterDAO) DataAccessFactory.dataHolder().get("registerDAO");

	@Override
	public AppResponse process(Map<String, String> map, String content, long requestIndex) {
		String mobile = map.get("telephone");
		String thirdId = map.get("thirdid");
		String uuid = map.get("uuid");
		String channelCode = map.get("channelno");

		AppResponse appResponse = new AppResponse();
		appResponse.setCode(EnumConstants.CALENDAR_SUCCESS_200);

		RegisterInfo registerInfo = null;
		Map<String, String> registerMap = new HashMap<String, String>();

		do {
			if (!ParameterValidation.validationChannelno(channelCode)) {
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : channel code is null, return 400");
				break;
			}
			if (ParameterValidation.validationMobile(mobile)) {/// 通过手机号码注册登录
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_501);
				logger.info(requestIndex + " : user register by mobile, no support, return 501");

			} else if (ParameterValidation.validationThirdIdOrUuid(thirdId)) {/// 通过第三方ID注册登录
				registerInfo = registerDAO.getRegisterInfo(thirdId, "third_id");
				if (registerInfo != null) {
					registerMap.put("uid", registerInfo.getUid());
					registerMap.put("status", EnumConstants.CALENDAR_REGISTER_FINISHED);
					appResponse.setRespContent(JSON.toJSONString(registerMap));
					logger.info(requestIndex + " : user have registered already by third id");
					break;
				}

				/// 找不到记录，可能是用户第一次通过第三方ID登录，之前用uuid登录
				int flag = 1;
				// if(uuid != null && uuid.length() > 4){
				// registerInfo = registerDAO.getRegisterInfo(uuid, "uuid");
				// if(registerInfo != null){
				// int registerIndex =
				// CalendarUtil.getRegisterTableIndex(thirdId);
				// int uuidIndex = CalendarUtil.getRegisterTableIndex(uuid);
				//
				// if(registerIndex != uuidIndex){
				// registerInfo.setThirdId(thirdId);
				// registerDAO.saveRegisterInfo(registerInfo);
				// }else{
				// registerDAO.updateThirdIdByUuid(thirdId, uuid);
				// }
				//
				// registerMap.put("uid", registerInfo.getUid());
				// registerMap.put("status",
				// EnumConstants.CALENDAR_REGISTER_RELATED_OK);
				// appResponse.setRespContent(JSON.toJSONString(registerMap));
				// logger.info(requestIndex + " : user have registered already
				// by uuid, and third id first related to the uuid");
				// break;
				// }else{///找不到记录，可能是用户第一次通过第三方ID登录，之前也没有用过uuid登录
				// flag = 1;
				// }
				// }else{
				// flag = 2;//uuid无效
				// }

				/// 为第三方ID生成新记录
				registerInfo = new RegisterInfo();
				if (flag == 1) {
					registerInfo.setUuid(uuid);
				}
				registerInfo.setThirdId(thirdId);
				registerInfo.setStatus(1);
				registerInfo.setChannelCode(channelCode);
				registerInfo.setCreateTime(System.currentTimeMillis());
				String uid = CalendarUtil.generateUid(thirdId);
				registerInfo.setUid(uid);
				registerDAO.saveRegisterInfo(registerInfo);

				int dbIndex = CalendarUtil.getDbIndex(uid);
				String dataSource = "dataSource" + dbIndex;
				DBContextHolder.setDBType(dataSource);

				AuthAccount authAccount = new AuthAccount();
				authAccount.setUid(uid);
				authAccount.setStatus(1);
				authAccount.setChannelCode(channelCode);
				authAccount.setCreateTime(System.currentTimeMillis());
				userDAO.SaveAuthAccount(authAccount);
				DBContextHolder.clearDBType();

				registerMap.put("uid", uid);
				registerMap.put("status", EnumConstants.CALENDAR_REGISTER_OK);
				appResponse.setRespContent(JSON.toJSONString(registerMap));
				logger.info(requestIndex + " : user first register by third id, return uid = " + uid);

			} else if (ParameterValidation.validationThirdIdOrUuid(uuid)) {
				registerInfo = registerDAO.getRegisterInfo(uuid, "uuid");
				if (registerInfo != null) {
					registerMap.put("uid", registerInfo.getUid());
					registerMap.put("status", EnumConstants.CALENDAR_REGISTER_FINISHED);
					appResponse.setRespContent(JSON.toJSONString(registerMap));
					logger.info(requestIndex + " : user have registered already by uuid");
					break;
				}
				/// 找不到记录，用户首次通过uuid注册登录
				/// 为uuid生成新记录
				registerInfo = new RegisterInfo();
				registerInfo.setUuid(uuid);
				registerInfo.setStatus(1);
				registerInfo.setChannelCode(channelCode);
				registerInfo.setCreateTime(System.currentTimeMillis());
				String uid = CalendarUtil.generateUid(uuid);
				registerInfo.setUid(uid);
				registerDAO.saveRegisterInfo(registerInfo);

				int dbIndex = CalendarUtil.getDbIndex(uid);
				String dataSource = "dataSource" + dbIndex;
				DBContextHolder.setDBType(dataSource);

				AuthAccount authAccount = new AuthAccount();
				authAccount.setUid(uid);
				authAccount.setStatus(1);
				authAccount.setChannelCode(channelCode);
				authAccount.setCreateTime(System.currentTimeMillis());
				userDAO.SaveAuthAccount(authAccount);
				DBContextHolder.clearDBType();

				registerMap.put("uid", uid);
				registerMap.put("status", EnumConstants.CALENDAR_REGISTER_OK);
				appResponse.setRespContent(JSON.toJSONString(registerMap));
				logger.info(requestIndex + " : user first register by uuid, return uid = " + uid);

			} else {/// 错误
				appResponse.setCode(EnumConstants.CALENDAR_ERROR_400);
				logger.error(requestIndex + " : param error, return 400");

			}
		} while (false);

		return appResponse;
	}

}
