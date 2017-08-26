package com.yitianyike.calendar.appserver.handler;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.common.EnumConstants;

/**
 * 用户登录请求
 * @author xujinbo
 *
 */
public class TestHandler {
    private static Logger logger = Logger.getLogger(TestHandler.class);
    
    private Map<String, String> parmMap;
    private ChannelHandlerContext ctx;
    private String content;
    
    public TestHandler(ChannelHandlerContext ctx, Map<String, String> map, String content) {
    	this.parmMap = map;
    	this.ctx = ctx;
    	this.content = content;
    }  

    void process() {
    	logger.info(EnumConstants.COLOR_RED + "test handler content : " + content + EnumConstants.COLOR_NONE);
        
		String response = "thank you!";
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,io.netty.handler.codec.http.HttpResponseStatus.OK, Unpooled.wrappedBuffer(response.getBytes()));
		ResponseGenerator.sendHttpResponse(ctx,  res);
		
		logger.info(EnumConstants.COLOR_RED + "test handler resp : " + response + EnumConstants.COLOR_NONE);
        
    }
    public static void main(String[] args) throws Exception {

    	List<String> list = new ArrayList<String>();
    	list.add("1");
    	list.add("2");
    	list.add("3");
    	String bbb = "";
    	int index = 0;
    	for(String aa: list){
    		
    		if(aa.equalsIgnoreCase("1")){
    			break;
    		}
    		index++;
    	}
    	list.remove(index);
    	
    	
    	String[] arr = bbb.split(",");
        List<String> list2 = java.util.Arrays.asList(arr);
        int i = 9;
        
         Map<String,Object> map = new HashMap<String,Object>();
         map.put("a", "value");
         map.put("b", "message");
         Map<String,Object> map1 = new HashMap<String,Object>();
         
         map.put("data", map1);
         map1.put("id", 1);
         map1.put("name", "beijing");
         
         String response = JSON.toJSONString(map);
        
         List<String> paramList = new ArrayList<String>();
         paramList.add("1");
         paramList.add("2");
         paramList.add("3");
         paramList.add("4");
         while(paramList.size() > 0){
     			if(paramList.size() == 1){
     				System.out.println(paramList.get(0));
     				break;
     			}if(paramList.size() == 2){
     				System.out.println(paramList.get(0) + "," + paramList.get(1));
     				break;
     			}if(paramList.size() == 3){
     				System.out.println(paramList.get(0) + "," + paramList.get(1) + "," + paramList.get(2));
     				break;
     			}else{
     				System.out.println(paramList.get(0) + "," + paramList.get(1) + "," + paramList.get(2));
     				
     				paramList = paramList.subList(3, paramList.size());
     				int n = 0;
     			}
     	}
         
         final List<Integer> ss = new ArrayList<Integer>();
         ss.add(0);
         System.out.println(ss.get(0));
         
         
         Calendar cal = Calendar.getInstance();

         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

         //String mDateTime=formatter.format(new Date(System.currentTimeMillis()));
         Date dd = new Date(1556098712);
         String mDateTime=formatter.format(dd);

         System.out.println(cal.getTime() + "|" + mDateTime);
         
         String tempString = "[ddd]";
         if(tempString.charAt(0) == '[' && tempString.charAt(tempString.length()-1) == ']'){
				tempString = tempString.substring(1, tempString.length()-1);
			}
         System.out.println(tempString);
         
         List<Object> myList = null;
         List<String> strs = (List<String>)(List)myList;
         
         String strr = "u123d 23 3322";
         String check = strr.replaceAll(" ", "") + "1213242323";
 		boolean result = check.matches("^u[0-9]+");
 		if (result == true) {
 		    int gg = 0;
 		}
 		int x=(int)(Math.random()*1000);
 		System.out.println("x="+x);
 		x = (int)(Math.random()*100000000);
 		System.out.println("x="+x);
 		
 		System.out.println(new Date());
 		Calendar cd = Calendar.getInstance();  
 		SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US);  
 		sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT  
 		String str = sdf.format(cd.getTime());  
 		System.out.println(str);
 		
         int k = 0;
    }
}
