package net.sllmdilab.t5xrefmanager.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import net.sllmdilab.commons.domain.SqlDevice;
import net.sllmdilab.commons.domain.SqlObservation;

@Repository
public class ObservationSqlDao {
	private static final Logger logger = LoggerFactory.getLogger(ObservationSqlDao.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<Code> searchCodesByDevice(String deviceId, Date start, Date end) {
		//@formatter:on
		String query = "SELECT\n" + 
				"DISTINCT " + 
				"code, " + 
				"code_system " + 
				"FROM " + 
				"t5_observation " + 
				"JOIN " + 
				"t5_device " + 
				"ON " + 
				"t5_observation.id = t5_device.observation_id " + 
				"WHERE " + 
				"start_time BETWEEN ? AND ? " + 
				"AND device_id = ? ";
		//@formatter:off
		
		long queryStartMilli = System.currentTimeMillis();
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(query,
				Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR);
		PreparedStatementCreator psc = pscf
				.newPreparedStatementCreator(Arrays.asList(start, end, deviceId));
		List<Code> result = jdbcTemplate.query(psc, new SummaryRowMapper());
		logger.debug("Fetching summary from SQL DB took " + (System.currentTimeMillis() - queryStartMilli));
		
		return result;
	}

	public List<SqlObservation> searchByDevice(String deviceId, String observationTypeCode, Date start, Date end) {
		List<SqlObservation> result = new ArrayList<>();
		//@formatter:off
		String query = 
			"SELECT " +
			"t5_observation.id, " + 
			"message_id, " + 
			"uid, " + 
			"set_id, " + 
			"start_time, " + 
			"end_time, " + 
			"value, " + 
			"value_type, " + 
			"code, " + 
			"code_system, " + 
			"unit, " + 
			"unit_system, " + 
			"sample_rate, " + 
			"data_range, " +
			"array_agg(device_id) AS device_id_list, " +
			"array_agg(level) AS device_level_list " +
			"FROM " +
			"t5_observation " + 
			"JOIN " +
			"t5_device " +
			"ON " +
			"t5_observation.id = t5_device.observation_id "+
			"WHERE " +
			"code = ? " +
			"AND " +
			"start_time BETWEEN ? AND ? " +
			"AND " +
			"device_id = ? " + 
			"GROUP BY " + 
			"t5_observation.id, " + 
			"message_id, " + 
			"uid, " + 
			"set_id, " + 
			"start_time, " + 
			"end_time, " + 
			"value, " + 
			"value_type, " + 
			"code, " + 
			"code_system, " + 
			"unit, " + 
			"unit_system, " + 
			"sample_rate, " + 
			"data_range " +
			"ORDER BY start_time ";
		//@formatter:on

		long queryStartMilli = System.currentTimeMillis();
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(query, Types.VARCHAR,
				Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR);
		PreparedStatementCreator psc = pscf
				.newPreparedStatementCreator(Arrays.asList(observationTypeCode, start, end, deviceId));
		result = jdbcTemplate.query(psc, new ObservationRowMapper());
		logger.debug("Fetching from SQL DB took " + (System.currentTimeMillis() - queryStartMilli));
		return result;
	}

	public static class ObservationRowMapper implements RowMapper<SqlObservation> {

		@Override
		public SqlObservation mapRow(ResultSet rs, int rowNum) throws SQLException {
			SqlObservation obs = new SqlObservation();
			obs.setId(rs.getLong("id"));
			obs.setMessageId(rs.getLong("message_id"));
			obs.setUid(rs.getString("uid"));
			obs.setSetId(rs.getString("set_id"));
			obs.setStartTime(rs.getTimestamp("start_time"));
			obs.setEndTime(rs.getTimestamp("end_time"));
			obs.setValue(rs.getString("value"));
			obs.setValueType(rs.getString("value_type"));
			obs.setCode(rs.getString("code"));
			obs.setCodeSystem(rs.getString("code_system"));
			obs.setUnit(rs.getString("unit"));
			obs.setUnitSystem(rs.getString("unit_system"));
			obs.setSampleRate(rs.getString("sample_rate"));
			obs.setDataRange(rs.getString("data_range"));

			String[] deviceIds = (String[]) rs.getArray("device_id_list").getArray();
			String[] deviceLevels = (String[]) rs.getArray("device_level_list").getArray();
			obs.setDevices(createSqlDevices(obs.getId(), deviceIds, deviceLevels));
			return obs;
		}

		private List<SqlDevice> createSqlDevices(long observationId, String[] deviceIds, String[] deviceLevels) {
			List<SqlDevice> devices = new ArrayList<>(deviceIds.length);
			for (int i = 0; i < deviceIds.length; ++i) {
				SqlDevice device = new SqlDevice();
				device.setDeviceId(deviceIds[i]);
				device.setLevel(deviceLevels[i]);
				device.setObservationId(observationId);

				devices.add(device);
			}
			return devices;
		}
	}
	
	public static class Code {
		public final String code;
		public final String codeSystem;
		
		public Code(String code, String codeSystem) {
			this.code = code;
			this.codeSystem = codeSystem;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result + ((codeSystem == null) ? 0 : codeSystem.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Code other = (Code) obj;
			if (code == null) {
				if (other.code != null)
					return false;
			} else if (!code.equals(other.code))
				return false;
			if (codeSystem == null) {
				if (other.codeSystem != null)
					return false;
			} else if (!codeSystem.equals(other.codeSystem))
				return false;
			return true;
		}
		
	}
	
	private static class SummaryRowMapper implements RowMapper<Code> {
		@Override
		public Code mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Code(rs.getString("code"), rs.getString("code_system"));
		}
		
	}
}
