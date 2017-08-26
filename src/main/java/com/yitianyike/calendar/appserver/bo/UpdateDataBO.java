/**
 * 
 */
package com.yitianyike.calendar.appserver.bo;

import java.util.Map;

import com.yitianyike.calendar.appserver.model.response.AppResponse;

/**
 * @author xujinbo
 *
 */
public interface UpdateDataBO {

   AppResponse process(Map<String, String> map, String content, long requestIndex);
   
   AppResponse updateByDate(Map<String, String> map, String content, long requestIndex);
}
