package com.yitianyike.calendar.appserver.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class CalendarUtil {

	private static long requestIndex = 0;
	
	private static long idIndex = 1000;
	
	private static String ipSuffix = "000";
	static{
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip=addr.getHostAddress().toString();
			ipSuffix = ip.substring(ip.lastIndexOf('.') + 1);
			System.out.println("ip:" + ip + ", suffix:"+ ipSuffix);
			if(ipSuffix.length() == 2){
				ipSuffix = "0" + ipSuffix;
			}else if(ipSuffix.length() == 1){
				ipSuffix = "00" + ipSuffix;
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static synchronized long getRequestIndex(){
		return requestIndex++;
	}
	
	public static synchronized long getIdIndex(){
		idIndex = idIndex % 10000;
		if(idIndex == 0){
			idIndex = 1000;
		}
		return idIndex++;
	}
	
	public static int getRegisterTableIndex(String string){
    	if(string.length() < 4){
    		return 0;
    	}
    	
    	String str = string.substring(string.length() - 4);
    	int i = Math.abs((str.charAt(0) - 48)) * 1000 + Math.abs((str.charAt(1) - 48 )) * 100 + Math.abs((str.charAt(2) - 48)) * 10 + Math.abs((str.charAt(3) - 48));
    	
    	return i%(PropertiesUtil.dbRegisterTables);  	
    }
   
	public static int getDbIndex(String string){
		if(string.length() < 8){
    		return 0;
    	}
		String str = string.substring(string.length() - 8);
		str = str.substring(0, 2);
		return Math.abs((str.charAt(0) - 48)) * 10 + Math.abs((str.charAt(1) - 48));
	}
	public static int getUserTableIndex(String string){
		if(string.length() < 8){
    		return 0;
    	}
		String str = string.substring(string.length() - 8);
		str = str.substring(2, 4);
		return Math.abs((str.charAt(0) - 48)) * 10 + Math.abs((str.charAt(1) - 48));
	}
	public static int getSubscribeTableIndex(String string){
		if(string.length() < 8){
    		return 0;
    	}
		String str = string.substring(string.length() - 8);
		str = str.substring(4, 6);
		return Math.abs((str.charAt(0) - 48)) * 10 + Math.abs((str.charAt(1) - 48));
	}
	public static int getRedisIndex(String string){
		if(string.length() < 8){
    		return 0;
    	}
		String str = string.substring(string.length() - 8);
		str = str.substring(6, 8);
		return Math.abs((str.charAt(0) - 48)) * 10 + Math.abs((str.charAt(1) - 48));
	}
	public static String generateUid(String id){
		
//		UUID uuid = UUID.randomUUID();  
//        String str = uuid.toString();  
//        // 去掉"-"符号  
//        String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);  
//        //System.out.println(temp);
        
        int x=(int)(Math.random()*10000000);
        
        String uid = "u" + ipSuffix + getIdIndex() + System.currentTimeMillis() + x + "1234567890";
        uid = uid.substring(0, 24);
        
        String dbNo = PropertiesUtil.dbNo >= 10 ?  "" + PropertiesUtil.dbNo : "0" + PropertiesUtil.dbNo;
        String dbUserNo = PropertiesUtil.dbUserNo >= 10 ?  "" + PropertiesUtil.dbUserNo : "0" + PropertiesUtil.dbUserNo;
        String dbSubscribeNo = PropertiesUtil.dbSubscribeNo >= 10 ?  "" + PropertiesUtil.dbSubscribeNo : "0" + PropertiesUtil.dbSubscribeNo;
        String redisNo = PropertiesUtil.redisNo >= 10 ?  "" + PropertiesUtil.redisNo : "0" + PropertiesUtil.redisNo;
        uid = uid + dbNo + dbUserNo + dbSubscribeNo + redisNo;
		return uid;
	}
	public static String generateToken(String uid){
		
		//UUID uuid = UUID.randomUUID();  
        //String str = uuid.toString();  
        // 去掉"-"符号  
        //String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);  
        //System.out.println(temp);
        
        int x=(int)(Math.random()*10000000);
        
        String token = "t" + ipSuffix + getIdIndex() + System.currentTimeMillis() + x + "1234567890";
        token = token.substring(0, 24);
        
        token = token + uid.substring(24);
        
		return token;
	}
}
