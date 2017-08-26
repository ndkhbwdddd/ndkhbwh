package com.yitianyike.calendar.appserver.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.yitianyike.calendar.appserver.dao.DataDAO;
import com.yitianyike.calendar.appserver.model.DataInfo;


public class DataDAOImpl extends BaseDAO implements DataDAO {

	@Override
	public List<DataInfo> getDataInfos(String key) {
		
		StringBuilder selectSql = new StringBuilder();
		selectSql.append("select * from data_cache ");
		selectSql.append(" where cache_key = :cache_key ");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();

		paramMap.put("cache_key", key);
		
		List<DataInfo> dataInfos = null;
		try{
			dataInfos = this.getNamedParameterJdbcTemplate().
					query(selectSql.toString(), paramMap, new RowMapper<DataInfo>() {
						@Override
					public DataInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						DataInfo dataInfo = new DataInfo();
						dataInfo.setCacheKey(rs.getString("cache_key"));
						dataInfo.setField(rs.getString("field"));
						dataInfo.setCacheValue(rs.getString("cache_value"));
						dataInfo.setUpdateTime(rs.getLong("update_time"));
						dataInfo.setCreateTime(rs.getLong("create_time"));
						return dataInfo;
					}
				});
		}catch(Exception e){
			e.printStackTrace();
		}
		return dataInfos;
		
	}


}
