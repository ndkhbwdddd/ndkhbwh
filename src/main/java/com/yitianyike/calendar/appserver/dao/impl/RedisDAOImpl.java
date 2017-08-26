/**
 * 
 */
package com.yitianyike.calendar.appserver.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.yitianyike.calendar.appserver.dao.RedisDAO;
import com.yitianyike.calendar.appserver.service.DataAccessFactory;

/**
 * @author xujinbo
 *
 */
public class RedisDAOImpl implements RedisDAO {

	private static final Logger logger = Logger.getLogger(RedisDAOImpl.class.getName());

	// private RedisTemplate<String, Object> redisTemplate;
	private static final ThreadLocal<RedisTemplate<String, Object>> contextHolder = new ThreadLocal<RedisTemplate<String, Object>>();

	@SuppressWarnings("unchecked")
	@Override
	public void setRedisTemplate(String templateString) {
		// TODO Auto-generated method stub
		RedisDAOImpl.contextHolder
				.set((RedisTemplate<String, Object>) DataAccessFactory.getRedisCtxXml().getBean(templateString));
	}

	@Override
	public void clearRedisTemplate() {
		// TODO Auto-generated method stub
		RedisDAOImpl.contextHolder.remove();
	}

	@Override
	public int processTokenAndUid(final String uid, final String newToken, final String oldToken,
			final Map<String, String> map, final String time) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] uidKeyb = uid.getBytes();
				byte[] newTokenKeyb = newToken.getBytes();
				int seconds = Integer.parseInt(time);

				try {
					connection.multi();
					connection.hSet(newTokenKeyb, "uid".getBytes(), uidKeyb);
					if (oldToken == null) {
						for (Entry<String, String> entry : map.entrySet()) {
							connection.hSet(uidKeyb, entry.getKey().getBytes(), entry.getValue().getBytes());
						}
					} else {
						connection.hSet(uidKeyb, "token".getBytes(), newTokenKeyb);
					}

					if (seconds > 0) {
						connection.expire(uidKeyb, seconds);
						connection.expire(newTokenKeyb, seconds);
					}
					if (oldToken != null && oldToken.length() >= 8) {
						byte[] oldTokenKeyb = oldToken.getBytes();
						// connection.del(oldTokenKeyb);
						connection.expire(oldTokenKeyb, 5);
					}
					connection.exec();
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int processTokenAndUidForMulti(final String uid, final String newToken, final String oldToken,
			final Map<String, String> map, final String time) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] uidKeyb = uid.getBytes();
				byte[] newTokenKeyb = newToken.getBytes();
				int seconds = Integer.parseInt(time);

				try {
					connection.multi();
					connection.hSet(newTokenKeyb, "uid".getBytes(), uidKeyb);
					if (oldToken == null) {
						for (Entry<String, String> entry : map.entrySet()) {
							connection.hSet(uidKeyb, entry.getKey().getBytes(), entry.getValue().getBytes());
						}
					} else {
						connection.hSet(uidKeyb, "token".getBytes(), newTokenKeyb);
					}

					if (seconds > 0) {
						connection.expire(uidKeyb, seconds);
						connection.expire(newTokenKeyb, seconds);
					}
					connection.exec();
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int hsetColumnList(final String uid, final List<String> columnList) {

		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = uid.getBytes();

				String listStr = "";
				for (String str : columnList) {
					listStr += str + ",";
				}
				byte[] listb = listStr.getBytes();

				try {
					connection.hSet(keyb, "list".getBytes(), listb);
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public String hGetValue(final String key, final String field) {
		final List<String> valueList = new ArrayList<String>();
		contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();
				byte[] fieldb = field.getBytes();

				try {
					byte[] valueb = connection.hGet(keyb, fieldb);

					if (valueb != null) {
						valueList.add(new String(valueb));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				return 1L;
			}
		});
		if (valueList.size() == 0) {
			return null;
		} else {
			return valueList.get(0);
		}
	}

	@Override
	public int hSetValue(final String key, final String field, final String value) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();
				byte[] fieldb = field.getBytes();
				byte[] valueb = value.getBytes();

				try {
					connection.hSet(keyb, fieldb, valueb);
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public List<String> hMgetValue(final String key, final List<String> fieldList) {

		List<Object> myList = null;
		final RedisSerializer<String> serializer = contextHolder.get().getStringSerializer();
		myList = contextHolder.get().executePipelined(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection) throws DataAccessException {

				if (fieldList == null || fieldList.size() == 0) {
					return null;
				}
				byte[] keyb = key.getBytes();

				try {
					for (String field : fieldList) {
						connection.hGet(keyb, field.getBytes());
					}
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				return null;
			}
		}, serializer);

		List<String> strs = (List<String>) (List) myList;
		return strs;

		// final List<String> valueList = new ArrayList<String>();
		// contextHolder.get().execute(new RedisCallback<Long>() {
		// public Long doInRedis(RedisConnection connection)
		// throws DataAccessException {
		//
		// if(fieldList == null || fieldList.size() == 0){
		// return 1L;
		// }
		// byte[] keyb = key.getBytes();
		//
		// List<String> paramList = new ArrayList<String>(fieldList);
		//
		// while(paramList.size() > 0){
		// if(paramList.size() == 1){
		// byte[] valueb = connection.hGet(keyb, paramList.get(0).getBytes());
		// if(valueb != null){
		// valueList.add(new String(valueb));
		// }
		// break;
		// }else if(paramList.size() == 2){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 3){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 4){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 5){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 6){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 7){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes(),
		// paramList.get(6).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 8){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes(),
		// paramList.get(6).getBytes(), paramList.get(7).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 9){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes(),
		// paramList.get(6).getBytes(), paramList.get(7).getBytes(),
		// paramList.get(8).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else if(paramList.size() == 10){
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes(),
		// paramList.get(6).getBytes(), paramList.get(7).getBytes(),
		// paramList.get(8).getBytes(), paramList.get(9).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// break;
		// }else{
		// List<byte[]> byteList = connection.hMGet(keyb,
		// paramList.get(0).getBytes(), paramList.get(1).getBytes(),
		// paramList.get(2).getBytes(), paramList.get(3).getBytes(),
		// paramList.get(4).getBytes(), paramList.get(5).getBytes(),
		// paramList.get(6).getBytes(), paramList.get(7).getBytes(),
		// paramList.get(8).getBytes(), paramList.get(9).getBytes());
		// if(byteList != null){
		// for(byte[] bb: byteList){
		// if(bb != null){
		// valueList.add(new String(bb));
		// }
		// }
		// }
		// paramList = paramList.subList(10, paramList.size());
		// }
		// }
		//
		// return 1L;
		// }
		// });
		// return valueList;
	}

	@Override
	public int hMsetValue(final String key, final Map<String, String> map, final String time) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();
				int seconds = Integer.parseInt(time);

				try {
					connection.multi();
					for (Entry<String, String> entry : map.entrySet()) {
						connection.hSet(keyb, entry.getKey().getBytes(), entry.getValue().getBytes());
					}
					if (seconds > 0) {
						connection.expire(keyb, seconds);
					}
					connection.exec();
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public Map<String, String> hGetAll(final String key) {
		final Map<String, String> hashMap = new HashMap<String, String>();
		contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {

					Map<byte[], byte[]> map = connection.hGetAll(keyb);
					for (Entry<byte[], byte[]> entry : map.entrySet()) {
						String key = new String(entry.getKey());
						String value = new String(entry.getValue());
						hashMap.put(key, value);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				return 1L;
			}
		});
		return hashMap;
	}

	@Override
	public int keyExist(final String key) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					Boolean exist = connection.exists(keyb);
					if (exist) {
						return 1L;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return ret.intValue();
	}

	@Override
	public List<String> zRevRangeByScoreWithScores(final String key, final int min, final int max) {
		final List<String> valueList = new ArrayList<String>();
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					Set<Tuple> set = connection.zRevRangeByScoreWithScores(keyb, max, min);
					if (set != null) {
						for (Tuple tu : set) {
							valueList.add(new String(tu.getValue()));
							// System.out.println("score:" + tu.getScore() + ",
							// value:" + new String(tu.getValue()));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return valueList;
	}

	@Override
	public List<String> zRangeByScoreWithScores(final String key, final int min, final int max) {
		final List<String> valueList = new ArrayList<String>();
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					Set<Tuple> set = connection.zRangeByScoreWithScores(keyb, min, max);
					if (set != null) {
						for (Tuple tu : set) {
							valueList.add(new String(tu.getValue()));
							// System.out.println("score:" + tu.getScore() + ",
							// value:" + new String(tu.getValue()));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return valueList;
	}

	@Override
	public int zAdd(final String key, final Map<String, String> map) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					connection.multi();
					for (Entry<String, String> entry : map.entrySet()) {
						connection.zAdd(keyb, Integer.parseInt(entry.getKey()), entry.getKey().getBytes());
					}

					connection.exec();
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int hMsetAndZadd(final String key, final Map<String, String> map) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();
				//byte[] zkeyb = (key + "-zset").getBytes();

				try {
					connection.multi();
					for (Entry<String, String> entry : map.entrySet()) {
						connection.hSet(keyb, entry.getKey().getBytes(), entry.getValue().getBytes());
						//connection.zAdd(zkeyb, Integer.parseInt(entry.getKey()), entry.getKey().getBytes());
					}

					connection.exec();
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int delKey(final String key) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					connection.del(keyb);
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int sAddSetValue(final String deviceUid, final String devicetoken) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = deviceUid.getBytes();
				byte[] valueb = devicetoken.getBytes();
				try {
					connection.sAdd(keyb, valueb);
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int sSetValueExist(final String deviceUid, final String devicetoken) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = deviceUid.getBytes();
				byte[] valueb = devicetoken.getBytes();
				try {
					Boolean exist = connection.sIsMember(keyb, valueb);
					if (exist) {
						return 1L;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return ret.intValue();
	}

	@Override
	public int delSetValue(final String deviceUid, final String devicetoken) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = deviceUid.getBytes();
				byte[] valueb = devicetoken.getBytes();

				try {
					connection.sRem(keyb, valueb);
				} catch (Exception e) {
					e.printStackTrace();
					return 0L;
				}

				return 1L;
			}
		});
		return ret.intValue();
	}

	@Override
	public Set<String> sGetAll(final String key) {
		final Set<String> set = new HashSet<String>();
		contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = key.getBytes();

				try {
					Set<byte[]> keys = connection.sMembers(keyb);
					for (byte[] bs : keys) {
						String value = new String(bs);
						set.add(value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return set;
	}

	@Override
	public int sSetCard(final String deviceUid) {
		Long ret = contextHolder.get().execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = deviceUid.getBytes();
				try {
					Long sCard = connection.sCard(keyb);
					return sCard;
				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0L;
			}
		});
		return ret.intValue();
	}

	// @Override
	// public String getValue(final String aidKey) {
	// final List<String> list = new ArrayList<String>();
	// contextHolder.get().execute(new RedisCallback<Long>() {
	// public Long doInRedis(RedisConnection connection) throws
	// DataAccessException {
	//
	// byte[] keyb = aidKey.getBytes();
	//
	// try {
	// byte[] bs = connection.get(keyb);
	// if (bs != null) {
	// String value = new String(bs);
	// list.add(value);
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return 0L;
	// }
	// });
	// if (!list.isEmpty()) {
	// return list.get(0);
	// } else {
	// return null;
	// }
	// }
	@Override
	public String getValue(final String aidKey) {

		String ret = contextHolder.get().execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection) throws DataAccessException {

				byte[] keyb = aidKey.getBytes();
				String value = null;
				try {
					byte[] bs = connection.get(keyb);

					if (bs != null)
						value = new String(bs);

				} catch (Exception e) {
					e.printStackTrace();
				}

				return value;
			}
		});
		return ret;
	}

}
