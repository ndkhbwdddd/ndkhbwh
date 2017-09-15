package com.yitianyike.calendar.appserver.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.yitianyike.calendar.appserver.dao.UserDAO;
import com.yitianyike.calendar.appserver.model.AuthAccount;
import com.yitianyike.calendar.appserver.model.DeviceInfo;
import com.yitianyike.calendar.appserver.model.UserSub;
import com.yitianyike.calendar.appserver.util.CalendarUtil;

@Component("userDAO")
public class UserDAOImpl extends BaseDAO implements UserDAO {

	@Override
	public AuthAccount getAccountInfo(String uid) {
		int userIndex = CalendarUtil.getUserTableIndex(uid);
		String userTable = "user_info" + userIndex;

		StringBuilder select = new StringBuilder();
		select.append("select * from " + userTable);
		select.append(" where uid = ? limit 1");
		AuthAccount accountInfo = null;
		try {
			accountInfo = this.getJdbcTemplate().queryForObject(select.toString(), new RowMapper<AuthAccount>() {
				@Override
				public AuthAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
					AuthAccount accountInfo = new AuthAccount();
					accountInfo.setUid(rs.getString("uid"));
					accountInfo.setChannelCode(rs.getString("channel_code"));
					accountInfo.setStatus(rs.getInt("status"));
					accountInfo.setCreateTime(rs.getLong("create_time"));
					return accountInfo;
				}
			}, uid);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return accountInfo;
	}

	@Override
	public int SaveAuthAccount(AuthAccount account) {
		int userIndex = CalendarUtil.getUserTableIndex(account.getUid());
		String userTable = "user_info" + userIndex;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + userTable + "(uid, channel_code, status, create_time) values(?, ?, ?, ?)");
		Object[] objs = new Object[] { account.getUid(), account.getChannelCode(), account.getStatus(),
				account.getCreateTime() };
		try {
			return this.getJdbcTemplate().update(sb.toString(), objs);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public List<String> getSubscribeList(String uid) {
		int subscribeIndex = CalendarUtil.getSubscribeTableIndex(uid);
		String subscribeTable = "user_subscribe" + subscribeIndex;

		Map<String, Object> paramMap = new HashMap<String, Object>();

		String selectSql = "select column_id from " + subscribeTable + " where uid = :uid ";
		paramMap.put("uid", uid);

		List<String> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String mi = "" + rs.getInt("column_id");
				return mi;
			}

		});
		return list;
	}

	@Override
	public List<Map<String, String>> getSubscribedList(String uid) {
		int subscribeIndex = CalendarUtil.getSubscribeTableIndex(uid);
		String subscribeTable = "user_subscribe" + subscribeIndex;

		Map<String, Object> paramMap = new HashMap<String, Object>();

		String selectSql = "select column_id,type from " + subscribeTable + " where uid = :uid ";
		paramMap.put("uid", uid);
		List<Map<String, String>> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap,
				new RowMapper<Map<String, String>>() {
					@Override
					public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
						Map<String, String> map = new HashMap<String, String>();
						String column_id = "" + rs.getInt("column_id");
						String type = "" + rs.getInt("type");
						map.put("column_id", column_id);
						map.put("type", type);
						return map;
					}

				});
		return list;
	}

	@Override
	public int saveSubscribeId(String uid, String columnId, int type) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;
		// 查看是否存在此订阅项,存在update不存在save
		int count = haveThisColumnId(uid, columnId, table);
		if (count > 0) {
			String deleteSql = "UPDATE " + table + " SET type=:type WHERE uid=:uid AND column_id=:column_id ";
			Map<String, Object> deleteMap = new HashMap<String, Object>();
			deleteMap.put("uid", uid);
			deleteMap.put("type", type);
			deleteMap.put("column_id", columnId);
			return this.getNamedParameterJdbcTemplate().update(deleteSql, deleteMap);

		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("insert into " + table + "(uid, type, column_id, create_time) values(?, ?, ?, ?)");
			Object[] objs = new Object[] { uid, type, columnId, System.currentTimeMillis() };
			try {
				return this.getJdbcTemplate().update(sb.toString(), objs);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

		}

	}

	private int haveThisColumnId(String uid, String columnId, String table) {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT id FROM " + table + " WHERE uid=:uid AND column_id=:column_id");
		paramMap.put("uid", uid);
		paramMap.put("column_id", columnId);
		List<String> list = this.getNamedParameterJdbcTemplate().query(sb.toString(), paramMap,
				new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet rs, int rowNum) throws SQLException {

						return rs.getString("id");
					}

				});
		return list.size();

	}

	@Override
	public int saveSubscribe(String uid, Set<Map<String, String>> sets) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + table + "(uid, type, column_id, create_time) values(?, ?, ?, ?)");

		List<Object[]> batchList = new ArrayList<Object[]>();
		for (Map<String, String> set : sets) {
			Object[] objs = new Object[] { uid, set.get("type"), set.get("column_id"), System.currentTimeMillis() };
			batchList.add(objs);
		}
		try {
			this.getJdbcTemplate().batchUpdate(sb.toString(), batchList);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int delSubscribeId(String uid, String columnId) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;

		String deleteSql = "delete from " + table + " where uid = :uid and column_id = :columnId ";
		Map<String, Object> deleteMap = new HashMap<String, Object>();
		deleteMap.put("uid", uid);
		deleteMap.put("columnId", columnId);
		return this.getNamedParameterJdbcTemplate().update(deleteSql, deleteMap);

	}

	@Override
	public int delSubscribeList(String uid) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;
		String deleteSql = "delete from " + table + " where uid = :uid";
		Map<String, Object> deleteMap = new HashMap<String, Object>();
		deleteMap.put("uid", uid);
		return this.getNamedParameterJdbcTemplate().update(deleteSql, deleteMap);
	}

	@Override
	public int delSubscribeIdByType(String uid, int type) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;

		String deleteSql = "delete from " + table + " where uid = :uid and type = :type ";
		Map<String, Object> deleteMap = new HashMap<String, Object>();
		deleteMap.put("uid", uid);
		deleteMap.put("type", type);
		return this.getNamedParameterJdbcTemplate().update(deleteSql, deleteMap);

	}

	@Override
	public DeviceInfo getDeviceInfo(String uid, String devicetoken) {
		int userIndex = CalendarUtil.getUserTableIndex(uid);
		String userTable = "user_device_info" + userIndex;

		StringBuilder select = new StringBuilder();
		select.append("select * from " + userTable);
		select.append(" where uid = ? and device_token = ? limit 1");
		DeviceInfo deviceInfo = null;
		try {
			deviceInfo = this.getJdbcTemplate().queryForObject(select.toString(), new RowMapper<DeviceInfo>() {
				@Override
				public DeviceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
					DeviceInfo deviceInfo = new DeviceInfo();
					deviceInfo.setStatus(rs.getInt("status"));
					deviceInfo.setUid(rs.getString("uid"));
					deviceInfo.setDeviceToken(rs.getString("device_token"));
					return deviceInfo;
				}
			}, uid, devicetoken);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return deviceInfo;
	}

	@Override
	public int updateDevicetokenStatus(String uid, String devicetoken, int status) {
		int userIndex = CalendarUtil.getUserTableIndex(uid);
		String userTable = "user_device_info" + userIndex;

		String update = "update " + userTable + " set status = :status "
				+ " where uid  = :uid and device_token= :device_token";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("status", status);
		paramMap.put("uid", uid);
		paramMap.put("device_token", devicetoken);
		return this.getNamedParameterJdbcTemplate().update(update, paramMap);
	}

	@Override
	public int saveDevicetoken(String uid, String devicetoken, int status) {
		int userIndex = CalendarUtil.getUserTableIndex(uid);
		String userTable = "user_device_info" + userIndex;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + userTable + "(uid, device_token, create_time,status) values(?, ?, ?, ?)");
		Object[] objs = new Object[] { uid, devicetoken, System.currentTimeMillis(), status };
		try {
			return this.getJdbcTemplate().update(sb.toString(), objs);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int updateDevicetokenStatusByUid(String uid, int status) {
		int userIndex = CalendarUtil.getUserTableIndex(uid);
		String userTable = "user_device_info" + userIndex;

		String update = "update " + userTable + " set status = :status " + " where uid  = :uid ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("status", status);
		paramMap.put("uid", uid);
		return this.getNamedParameterJdbcTemplate().update(update, paramMap);
	}

	@Override
	public int updateSubscribeId(String uid, String columnId, int type) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;

		String deleteSql = "UPDATE " + table + " SET type=:type WHERE uid=:uid AND column_id=:column_id ";
		Map<String, Object> deleteMap = new HashMap<String, Object>();
		deleteMap.put("uid", uid);
		deleteMap.put("type", type);
		deleteMap.put("column_id", columnId);
		return this.getNamedParameterJdbcTemplate().update(deleteSql, deleteMap);

	}

	@Override
	public List<UserSub> getSubscribeListIncludeType(String uid) {
		int subscribeIndex = CalendarUtil.getSubscribeTableIndex(uid);
		String subscribeTable = "user_subscribe" + subscribeIndex;

		Map<String, Object> paramMap = new HashMap<String, Object>();

		String selectSql = "select column_id,type from " + subscribeTable + " where uid = :uid ";
		paramMap.put("uid", uid);

		List<UserSub> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap, new RowMapper<UserSub>() {
			@Override
			public UserSub mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserSub us = new UserSub();
				String column_id = "" + rs.getInt("column_id");
				String type = "" + rs.getInt("type");
				us.setColumn_id(column_id);
				us.setType(type);
				return us;
			}

		});
		return list;
	}

	@Override
	public int batchSaveSubscribeId(String uid, Set<String> default_aids_valid, int i) {
		int index = CalendarUtil.getSubscribeTableIndex(uid);
		String table = "user_subscribe" + index;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + table + "(uid, type, column_id, create_time) values(?, ?, ?, ?)");

		List<Object[]> batchList = new ArrayList<Object[]>();

		for (String default_aid : default_aids_valid) {
			Object[] objs = new Object[] { uid, i, default_aid, System.currentTimeMillis() };
			batchList.add(objs);
		}

		try {
			this.getJdbcTemplate().batchUpdate(sb.toString(), batchList);
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
