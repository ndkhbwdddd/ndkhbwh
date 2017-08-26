package com.yitianyike.calendar.appserver.handler;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.yitianyike.calendar.appserver.bo.UnSubscribeBO;
import com.yitianyike.calendar.appserver.common.EnumConstants;
import com.yitianyike.calendar.appserver.model.response.AppResponse;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;
import com.yitianyike.calendar.appserver.util.AESUtil;
import com.yitianyike.calendar.appserver.util.CalendarUtil;
import com.yitianyike.calendar.appserver.util.StringEx;

/**
 * 用户登录请求
 * @author xujinbo
 *
 */
public class UnSubscribeHandler {
    private static Logger logger = Logger.getLogger(UnSubscribeHandler.class);
    private UnSubscribeBO unSubscribeBO = (UnSubscribeBO) DataAccessFactory.dataHolder().get("unSubscribeBO");

    private Map<String, String> parmMap;
    private ChannelHandlerContext ctx;
    private String content;
    
    public UnSubscribeHandler(ChannelHandlerContext ctx, Map<String, String> map, String content) {
    	this.parmMap = map;
    	this.ctx = ctx;
    	this.content = content;
    }  

    void process() {
    	long requestIndex = CalendarUtil.getRequestIndex();
    	
    	logger.info(EnumConstants.COLOR_BLUE + "unsubscribe handler param(" + requestIndex + ") : " + parmMap + EnumConstants.COLOR_NONE);
        
    	AppResponse resp = unSubscribeBO.process(parmMap, content, requestIndex);
    	
    	FullHttpResponse res = null;
    	String response = "";
    	String enResponse = null;
		if (resp.getCode() == EnumConstants.CALENDAR_SUCCESS_200) {
			enResponse = response = resp.getRespContent();
//			String aesKey = parmMap.get("aeskey");
//			if(aesKey != null){
//				try{
//					enResponse = AESUtil.Encrypt(response, aesKey);
//					enResponse = StringEx.MD5(enResponse + aesKey) + enResponse;
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}
			res = new DefaultFullHttpResponse(HTTP_1_1, io.netty.handler.codec.http.HttpResponseStatus.OK,
					Unpooled.wrappedBuffer(enResponse.getBytes()));
    	}else{
    		res = ResponseGenerator.getErrorResponse(resp.getCode());
    	}
    	ResponseGenerator.sendHttpResponse(ctx,  res);
		
    	String displayStr = response.length() > 1000 ? response.substring(0, 1000) + "......" : response;
		logger.info("unsubscribe handler resp(" + requestIndex + ") : length = (" + enResponse.length() + ") " + displayStr);
		
    }
    
}
