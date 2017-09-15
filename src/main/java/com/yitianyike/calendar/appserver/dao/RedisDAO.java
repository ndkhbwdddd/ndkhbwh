/**
 * 
 */
package com.yitianyike.calendar.appserver.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yitianyike.calendar.appserver.model.TokenInfo;

/**
 * @author xujinbo
 *
 */
public interface RedisDAO {

	void setRedisTemplate(String templateString);

	void clearRedisTemplate();

	//// 单点登录处理
	int processTokenAndUid(final String uid, final String newToken, final String oldToken,
			final Map<String, String> map, final String time);

	//// hash 操作
	int hsetColumnList(final String token, final List<String> columnList);

	String hGetValue(final String key, final String field);

	int hSetValue(final String key, final String field, final String value);

	List<String> hMgetValue(final String key, final List<String> fieldList);

	int hMsetValue(final String key, final Map<String, String> map, final String time);

	Map<String, String> hGetAll(final String key);

	//// zset 操作
	List<String> zRevRangeByScoreWithScores(final String key, int min, int max);

	List<String> zRangeByScoreWithScores(final String key, int min, int max);

	int zAdd(final String key, final Map<String, String> map);

	int hMsetAndZadd(final String key, final Map<String, String> map);

	//// 针对包列表，推荐列表，订阅列表，如果key不存在，需要从data_cache中读取整个列表数据到redis生成key
	//// 其它卡片数据，如果key不存在，直接返回，不需要其它操作
	int keyExist(final String key);

	int delKey(final String key);

	// 多点登录处理
	int processTokenAndUidForMulti(final String uid, final String newToken, final String oldToken,
			final Map<String, String> map, final String time);

	// set集合是否存在此value
	int sSetValueExist(String deviceUid, String devicetoken);

	// 存入set集合中值
	int sAddSetValue(final String deviceUid, final String devicetoken);

	// 删除set集合红的值
	int delSetValue(String string, String devicetoken);

	// 查看set集合中的数量
	int sSetCard(String deviceUid);

	// 获取set中所有的数据
	Set<String> sGetAll(final String key);

	// 根据key获取value
	String getValue(String aidKey);

	// 登录生成数据放入缓存
	int processTokenAndUidAndSubsForMulti(final String uid, final String token, final Map<String, String> uidMap,
			final String time);

}
