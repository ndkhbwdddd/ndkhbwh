package com.yitianyike.calendar.appserver.service;

public class DBContextHolder{  
//    public static final String DATA_SOURCE_FROM = "dataSourceFrom";  
//    public static final String DATA_SOURCE_TO = "dataSourceTo";  
//      
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();  
      
    public static void setDBType(String dbType) {  
        contextHolder.set(dbType);  
    }  
      
    public static String getDBType() {  
        return contextHolder.get();  
    }  
      
    public static void clearDBType() {  
        contextHolder.remove();  
    }  
}