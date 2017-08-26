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
public interface RegisterBO {

    AppResponse process(Map<String, String> map, String content, long requestIndex);

}
