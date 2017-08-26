package com.yitianyike.calendar.appserver.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.yitianyike.calendar.appserver.dao.RegisterDAO;
import com.yitianyike.calendar.appserver.model.RegisterInfo;
import com.yitianyike.calendar.appserver.util.CalendarUtil;

public class RegisterDAOImpl extends BaseDAO implements RegisterDAO {

	@Override
	public RegisterInfo getRegisterInfo(String userId, String field) {
		StringBuilder select = new StringBuilder();
		int RegisterIndex = CalendarUtil.getRegisterTableIndex(userId);
		String RegisterTable = "calendar_register.user_register" + RegisterIndex;
		select.append("select * from " + RegisterTable);
		select.append(" where " + field + " = ? limit 1");
		RegisterInfo accountInfo = null;
		try {
			accountInfo = this.getJdbcTemplate().queryForObject(select.toString(), new RowMapper<RegisterInfo>() {
				@Override
				public RegisterInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
					RegisterInfo accountInfo = new RegisterInfo();
					accountInfo.setUuid(rs.getString("uuid"));
					accountInfo.setThirdId(rs.getString("third_id"));
					accountInfo.setTelephone(rs.getString("telephone"));
					accountInfo.setUid(rs.getString("uid"));
					accountInfo.setPassword(rs.getString("password"));
					accountInfo.setStatus(rs.getInt("status"));
					accountInfo.setChannelCode(rs.getString("channel_code"));
					accountInfo.setUpdateTime(rs.getLong("update_time"));
					accountInfo.setCreateTime(rs.getLong("create_time"));
					return accountInfo;
				}
			}, userId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return accountInfo;
	}

	@Override
	public int updateThirdIdByUuid(String thirdId, String uuid) {
		int registerIndex = CalendarUtil.getRegisterTableIndex(uuid);
		String RegisterTable = "calendar_register.user_register" + registerIndex;

		String update = "update " + RegisterTable + " set third_id = :thirdId " + " where uuid  = :uuid ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("thirdId", thirdId);
		paramMap.put("uuid", uuid);
		return this.getNamedParameterJdbcTemplate().update(update, paramMap);
	}

	@Override
	public int saveRegisterInfo(RegisterInfo registerInfo) {
		int registerIndex = 0;
		if (registerInfo.getTelephone() != null && registerInfo.getTelephone().length() > 4) {
			registerIndex = CalendarUtil.getRegisterTableIndex(registerInfo.getTelephone());
		} else if (registerInfo.getThirdId() != null && registerInfo.getThirdId().length() > 4) {
			registerIndex = CalendarUtil.getRegisterTableIndex(registerInfo.getThirdId());
		} else {
			registerIndex = CalendarUtil.getRegisterTableIndex(registerInfo.getUuid());
		}
		String RegisterTable = "calendar_register.user_register" + registerIndex;

		StringBuilder sb = new StringBuilder();
		sb.append("insert into " + RegisterTable
				+ "(uuid, third_id, uid, telephone, password, channel_code, status, create_time) values(?, ?, ?, ?, ?, ?, ?, ?)");
		Object[] objs = new Object[] { registerInfo.getUuid(), registerInfo.getThirdId(), registerInfo.getUid(),
				registerInfo.getTelephone(), registerInfo.getPassword(), registerInfo.getChannelCode(),
				registerInfo.getStatus(), registerInfo.getCreateTime() };
		try {
			return this.getJdbcTemplate().update(sb.toString(), objs);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
