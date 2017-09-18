/**
 * 
 */
package com.yitianyike.calendar.appserver.bo;

import java.util.Map;

import com.yitianyike.calendar.appserver.model.response.AppResponse;

public interface ClearUserBO {

	AppResponse process(Map<String, String> map, String content, long requestIndex);

}
