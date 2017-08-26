package com.yitianyike.calendar.appserver.common;

public class EnumConstants {

	/**
	 * content type
	 */
	public final static String PLAIN_CONTENT_TYPE = "text/plain; charset=UTF-8";
	public final static String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";

	/**
	 * all request error id and info
	 */
	public static final int CALENDAR_SUCCESS_200 = 200;// 请求成功

	public static final int CALENDAR_REDIRECT_302 = 302;// 对象已移动
	public static final int CALENDAR_REDIRECT_304 = 304;// 未修改
	public static final int CALENDAR_REDIRECT_307 = 307;// 临时重定向

	public static final int CALENDAR_ERROR_400 = 400;// 请求无效
	public static final int CALENDAR_ERROR_401 = 401;// 未授权,登录失败
	public static final int CALENDAR_ERROR_403 = 403;// 禁止访问（用户数过多，客户端证书不受信任或无效，要求SSL等）
	public static final int CALENDAR_ERROR_404 = 404;// 无法找到文件
	public static final int CALENDAR_ERROR_405 = 405;// 资源被禁止
	public static final int CALENDAR_ERROR_406 = 406;// 无法接受
	public static final int CALENDAR_ERROR_407 = 407;// 要求代理身份验证
	public static final int CALENDAR_ERROR_410 = 410;// 永远不可用
	public static final int CALENDAR_ERROR_412 = 412;// 先决条件失败
	public static final int CALENDAR_ERROR_414 = 414;// 请求 - URI 太长

	public static final int CALENDAR_ERROR_500 = 500;// 内部服务器错误
	public static final int CALENDAR_ERROR_501 = 501;// 未实现
	public static final int CALENDAR_ERROR_502 = 502;// 网关错误

	/**
	 * register status
	 */
	public static final String CALENDAR_REGISTER_OK = "注册成功";
	public static final String CALENDAR_REGISTER_NO = "未注册";
	public static final String CALENDAR_REGISTER_FINISHED = "已经注册";
	public static final String CALENDAR_REGISTER_RELATED_OK = "已关联设备ID";
	public static final String CALENDAR_MEGER_OK = "合并成功";
	public static final String CALENDAR_UNBIND_OK = "解绑成功";
	public static final String CALENDAR_BIND_PUSH_OK = "绑定推送成功";
	public static final String CALENDAR_UNBIND_PUSH_OK = "解绑推送成功";
	public static final String CALENDAR_NOTBIND_PUSH_FINISHED = "未绑定推送";
	public static final String CALENDAR_DEKETE_SUB_OK = "清除订阅项成功";

	/**
	 * unsubscribe status
	 */
	public static final String CALENDAR_UNSUBSCRIBE_OK = "取消订阅成功";
	public static final String CALENDAR_UNSUBSCRIBE_NOEXISTS = "用户未订阅过该项";

	// /**
	// * message is read or not 0 : not read 1 : have read
	// */
	// public static final int MESSAGE_NOT_READ = 0;
	// public static final int MESSAGE_HAVE_READ = 1;
	//
	// /**
	// * minimum file size to compression.
	// */
	// public static final int COMPRESSION_STANDARD =
	// PropertiesUtil.getIntValue("compression.standard");
	//
	// /**
	// * 消息已读、未读状态 0： 未读 1： 已读
	// */
	// public static final byte MSG_UNREAD = 0;
	// public static final byte MSG_READED = 1;
	//
	// public static final int USER_STATUS_OFFLINE = 0;
	// public static final int USER_STATUS_ONLINE = 1;
	//
	// public static final int ATTACH_PIC = 1;
	// public static final int ATTACH_AUDIO = 2;
	// public static final int ATTACH_VIDEO = 3;
	//
	// public static final int USER_TYPE_CUSTOMER = 0;
	// public static final int USER_TYPE_SHOP = 1;
	//
	// public static final String USER_HEAD_IMG_URL =
	// PropertiesUtil.getValue("user.head.img.url"); // 用户头像URL
	// public static final String SHOP_HEAD_IMG_URL =
	// PropertiesUtil.getValue("shop.head.img.url"); // 商户头像URL
	//
	// public static final String SHOP_TYPE_FOR_PUSH_SERVICE = "1";
	// public static final String USER_TYPE_FOR_PUSH_SERVICE = "2";
	//
	//
	// //message type
	// public static final int MESSAGE_TYPE_SINGLE_CHAT = 0;
	// public static final int MESSAGE_TYPE_GROUP_CHAT = 1;
	// public static final int MESSAGE_TYPE_SYSTEM_MSG = 2;
	// public static final int MESSAGE_TYPE_ADD_FRIEND = 10;
	// public static final int MESSAGE_TYPE_VERIFY_FRIEND = 11;
	// public static final int MESSAGE_TYPE_DEL_FRIEND = 12;
	// public static final int MESSAGE_TYPE_CREATE_GROUP = 13;
	// public static final int MESSAGE_TYPE_ADD_GROUP = 14;
	// public static final int MESSAGE_TYPE_EXIT_GROUP = 15;
	// public static final int MESSAGE_TYPE_USER_INFO = 16;
	//
	// public static final String MESSAGE_VERIFY = "我通过了你的好友验证请求，现在我们可以开始聊天了";

	public static final String COLOR_NONE = "\033[0m";
	public static final String COLOR_BLACK = "\033[0;30m";
	public static final String COLOR_L_BLACK = "\033[1;30m";
	public static final String COLOR_RED = "\033[0;31m";
	public static final String COLOR_L_RED = "\033[1;31m";
	public static final String COLOR_GREEN = "\033[0;32m";
	public static final String COLOR_L_GREEN = "\033[1;32m";
	public static final String COLOR_BROWN = "\033[0;33m";
	public static final String COLOR_YELLOW = "\033[1;33m";
	public static final String COLOR_BLUE = "\033[0;34m";
	public static final String COLOR_L_BLUE = "\033[1;34m";
	public static final String COLOR_PURPLE = "\033[0;35m";
	public static final String COLOR_L_PURPLE = "\033[1;35m";
	public static final String COLOR_CYAN = "\033[0;36m";
	public static final String COLOR_L_CYAN = "\033[1;36m";
	public static final String COLOR_GRAY = "\033[0;37m";
	public static final String COLOR_WHITE = "\033[1;37m";
}
