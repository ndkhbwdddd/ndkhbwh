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
public interface MoreDataBO {

	AppResponse process(Map<String, String> map, String content,
			long requestIndex);

	AppResponse getSportsData(Map<String, String> parmMap, String content,
			long requestIndex);
}
