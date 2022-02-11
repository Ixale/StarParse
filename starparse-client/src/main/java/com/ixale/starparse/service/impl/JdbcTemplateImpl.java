package com.ixale.starparse.service.impl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class JdbcTemplateImpl extends NamedParameterJdbcTemplate {

	public JdbcTemplateImpl(DataSource dataSource) {
		super(dataSource);
	}

	public void execute(String sql) throws DataAccessException {
		this.getJdbcOperations().execute(sql);
	}

	public int update(String sql, Object... args) throws DataAccessException {
		return this.getJdbcOperations().update(sql, args);
	}

	public <T> T queryForObject(final String sql, final Class<T> requiredType) throws DataAccessException {
		return this.getJdbcOperations().queryForObject(sql, requiredType);
	}

	public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return this.getJdbcOperations().queryForObject(sql, rowMapper);
	}

	public <T> T query(final String sql, Object[] args, final ResultSetExtractor<T> rse) throws DataAccessException {
		return this.getJdbcOperations().query(sql, args, rse);
	}

	public <T> List<T> query(final String sql, Object[] args, final RowMapper<T> rowMapper) throws DataAccessException {
		return this.getJdbcOperations().query(sql, args, rowMapper);
	}

	public int[] batchUpdate(final String sql, final BatchPreparedStatementSetter bpse) {
		return this.getJdbcOperations().batchUpdate(sql, bpse);
	}
}
