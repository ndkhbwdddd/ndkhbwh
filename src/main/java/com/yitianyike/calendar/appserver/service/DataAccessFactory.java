package com.yitianyike.calendar.appserver.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yitianyike.calendar.appserver.bo.AllSubscribeListBO;
import com.yitianyike.calendar.appserver.bo.BindControlDriveCityBO;
import com.yitianyike.calendar.appserver.bo.BindPushBO;
import com.yitianyike.calendar.appserver.bo.ChannelCodesBO;
import com.yitianyike.calendar.appserver.bo.CurrentDataBO;
import com.yitianyike.calendar.appserver.bo.DeleteSubscribedListBO;
import com.yitianyike.calendar.appserver.bo.GetSportsDataDB;
import com.yitianyike.calendar.appserver.bo.IncrementDataBO;
import com.yitianyike.calendar.appserver.bo.LoginBO;
import com.yitianyike.calendar.appserver.bo.MergeSubscriptionsBO;
import com.yitianyike.calendar.appserver.bo.MoreDataBO;
import com.yitianyike.calendar.appserver.bo.RecommendSubscribeListBO;
import com.yitianyike.calendar.appserver.bo.RegisterBO;
import com.yitianyike.calendar.appserver.bo.SubscribeBO;
import com.yitianyike.calendar.appserver.bo.SubscribedListBO;
import com.yitianyike.calendar.appserver.bo.SynchronizeDataBO;
import com.yitianyike.calendar.appserver.bo.TabsListBO;
import com.yitianyike.calendar.appserver.bo.UnSubscribeBO;
import com.yitianyike.calendar.appserver.bo.UnbindBO;
import com.yitianyike.calendar.appserver.bo.UnbindPushBO;
import com.yitianyike.calendar.appserver.bo.UpdateDataBO;
import com.yitianyike.calendar.appserver.bo.UpdatePeriodDataBO;
import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.dao.UserDAO;

public class DataAccessFactory {

	private static Logger log = Logger.getLogger(DataAccessFactory.class);

	private static ApplicationContext mysqlDataCtxXml = null;
	private static ApplicationContext mysqlUserCtxXml = null;
	private static ApplicationContext mysqlRegisterCtxXml = null;
	private static ApplicationContext redisCtxXml = null;
	private static Map<String, Object> dataHolder = new HashMap<String, Object>();

	public static void initDataAccessMysqlDataByXML() {

		log.info("init Data Access Objects[Mysql Data] start...");
		mysqlDataCtxXml = new ClassPathXmlApplicationContext("spring-mysql-data.xml");
		dataHolder.put("dataDAO", (DataDAO) mysqlDataCtxXml.getBean("dataDAO"));
		log.info("init Data Access Objects[Mysql Data] over.");
	}

	public static void initDataAccessMysqlUserByXML() {

		log.info("init Data Access Objects[Mysql User] start...");
		mysqlUserCtxXml = new ClassPathXmlApplicationContext("spring-mysql-user.xml");

		dataHolder.put("userDAO", (UserDAO) mysqlUserCtxXml.getBean("userDAO"));

		dataHolder.put("loginBO", (LoginBO) mysqlUserCtxXml.getBean("loginBO"));

		dataHolder.put("registerBO", (RegisterBO) mysqlUserCtxXml.getBean("registerBO"));
		dataHolder.put("subscribeBO", (SubscribeBO) mysqlUserCtxXml.getBean("subscribeBO"));
		dataHolder.put("unSubscribeBO", (UnSubscribeBO) mysqlUserCtxXml.getBean("unSubscribeBO"));
		dataHolder.put("allSubscribeListBO", (AllSubscribeListBO) mysqlUserCtxXml.getBean("allSubscribeListBO"));
		dataHolder.put("recommendSubscribeListBO",
				(RecommendSubscribeListBO) mysqlUserCtxXml.getBean("recommendSubscribeListBO"));
		dataHolder.put("tabsListBO", (TabsListBO) mysqlUserCtxXml.getBean("tabsListBO"));
		dataHolder.put("subscribedListBO", (SubscribedListBO) mysqlUserCtxXml.getBean("subscribedListBO"));
		dataHolder.put("incrementDataBO", (IncrementDataBO) mysqlUserCtxXml.getBean("incrementDataBO"));
		dataHolder.put("updateDataBO", (UpdateDataBO) mysqlUserCtxXml.getBean("updateDataBO"));
		dataHolder.put("updatePeriodDataBO", (UpdatePeriodDataBO) mysqlUserCtxXml.getBean("updatePeriodDataBO"));
		dataHolder.put("mergeSubscriptionsBO", (MergeSubscriptionsBO) mysqlUserCtxXml.getBean("mergeSubscriptionsBO"));
		dataHolder.put("synchronizeDataBO", (SynchronizeDataBO) mysqlUserCtxXml.getBean("synchronizeDataBO"));
		dataHolder.put("unbindBO", (UnbindBO) mysqlUserCtxXml.getBean("unbindBO"));
		dataHolder.put("getSportsDataDB", (GetSportsDataDB) mysqlUserCtxXml.getBean("getSportsDataDB"));
		dataHolder.put("deleteSubscribedListBO",
				(DeleteSubscribedListBO) mysqlUserCtxXml.getBean("deleteSubscribedListBO"));
		dataHolder.put("bindPushBO", (BindPushBO) mysqlUserCtxXml.getBean("bindPushBO"));
		dataHolder.put("unbindPushBO", (UnbindPushBO) mysqlUserCtxXml.getBean("unbindPushBO"));
		dataHolder.put("currentDataBO", (CurrentDataBO) mysqlUserCtxXml.getBean("currentDataBO"));
		dataHolder.put("moreDataBO", (MoreDataBO) mysqlUserCtxXml.getBean("moreDataBO"));
		dataHolder.put("channelCodesBO", (ChannelCodesBO) mysqlUserCtxXml.getBean("channelCodesBO"));

		dataHolder.put("bindControlDriveCityBO",
				(BindControlDriveCityBO) mysqlUserCtxXml.getBean("bindControlDriveCityBO"));
		// TODO
		log.info("init Data Access Objects[Mysql User] over.");
	}

	public static void initDataAccessRedisByXML() {

		log.info("init Data Access Objects[Redis] start...");
		redisCtxXml = new ClassPathXmlApplicationContext("spring-redis.xml");
		dataHolder.put("redisDAO", (RedisDAO) redisCtxXml.getBean("redisDAO"));
		log.info("init Data Access Objects[Redis] over.");
	}

	public static void initDataAccessMysqlRegisterByXML() {
		log.info("init Data Access Objects[Mysql Register] start...");
		mysqlRegisterCtxXml = new ClassPathXmlApplicationContext("spring-mysql-register.xml");
		dataHolder.put("registerDAO", (RegisterDAO) mysqlRegisterCtxXml.getBean("registerDAO"));
		log.info("init Data Access Objects[Mysql Register] over.");

	}

	public static ApplicationContext getRedisCtxXml() {
		return redisCtxXml;
	}

	public static Map<String, Object> dataHolder() {
		return dataHolder;
	}

	public static void main(String[] args) {
		DataAccessFactory.initDataAccessMysqlUserByXML();
		// MessageDAO messageDAO =
		// (MessageDAO)DataAccessFactory.dataHolder.get("messageDAO");
		// List<String> removeIds = new ArrayList<String>();
		// removeIds.add("4600013860217351375867848705");
		// removeIds.add("4600013860217351375868558519");
		// removeIds.add("4600013860217351375869181472");
		// removeIds.add("4600013860217351375869191717");
		// messageDAO.removeQueue(removeIds,"","");
	}

}
