package com.yitianyike.calendar.appserver.util;

import org.apache.commons.lang.StringUtils;

public class ParameterValidation {

	// 手机号
	public static boolean validationMobile(String mobile) {
		return (StringUtils.isNotBlank(mobile) && mobile.matches("^1[0-9]{10}$"));
	};

	// 三方id和uuid
	public static boolean validationThirdIdOrUuid(String id) {
		return (StringUtils.isNotBlank(id) && id.replace("-", "").matches("^[A-Za-z0-9]{8,60}$"));
	};

	// uid和token
	public static boolean validationUidAndToken(String ut) {
		return (StringUtils.isNotBlank(ut) && ut.matches("^[u|t][A-Za-z0-9]{7,60}$"));
	};

	// uid和token
	public static boolean validationDevicetoken(String devicetoken) {
		return (StringUtils.isNotBlank(devicetoken) && devicetoken.matches("^[A-Za-z0-9]{7,60}$"));
	};
	
	
	// 渠道号
	public static boolean validationChannelno(String channelno) {
		return (StringUtils.isNotBlank(channelno) && channelno.matches("^[A-Za-z0-9]{1,20}$"));
	};


	// 版本号
	public static boolean validationVersion(String version) {
		return (StringUtils.isNotBlank(version) && version.matches("^[A-Za-z0-9]{1,10}$"));
	};

	// 订阅项集合,订阅项或其类型
	public static boolean validationAidsAndAidAndType(String at) {
		return (StringUtils.isNotBlank(at) && at.replace(" ", "").matches("[0-9]*"));
	};

	//验证num
	public static boolean validationNum(String at) {
		return (StringUtils.isNotBlank(at) && at.matches("[0-9]*"));
	};
	// 传递的时间
	public static boolean validationDateTime(String dateTime) {
		return (StringUtils.isNotBlank(dateTime) && dateTime.replace("-", "").matches("[0-9]{8}"));
	};

	// 验证状态
	public static boolean validationStatus(String status) {
		return (StringUtils.isNotBlank(status) && status.matches("[01]{1}"));
	};

}
