package com.yitianyike.calendar.appserver.service;

import org.apache.log4j.Logger;

/**
 * Application Container
 * 
 * Initialise Application by Configuration
 * 
 * @author xujinbo
 * 
 */
public class ApplicationConfig {

	private static Logger log = Logger.getLogger(ApplicationConfig.class
			.getName());

	/**
	 * Initialise Application by Configuration
	 */
	public static void init() {

		log.info("App init...");
		
		DataAccessFactory.initDataAccessMysqlDataByXML();
		DataAccessFactory.initDataAccessRedisByXML();
		DataAccessFactory.initDataAccessMysqlRegisterByXML();
		DataAccessFactory.initDataAccessMysqlUserByXML();
	}


	/**
	 * call before close service
	 */
	public static void release() {
	}
}
