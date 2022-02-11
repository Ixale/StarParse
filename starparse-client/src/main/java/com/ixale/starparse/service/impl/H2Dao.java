package com.ixale.starparse.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class H2Dao {

	private JdbcTemplateImpl jdbcTemplate;

	private static boolean isSchemaCreated = false;

	@Autowired
	public void setJdbcTemplate(JdbcTemplateImpl jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	protected JdbcTemplateImpl getJdbcTemplate() throws Exception {
		if (!isSchemaCreated) {
			InputStream in = getClass().getClassLoader().getResourceAsStream("db/schema.sql");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			jdbcTemplate.execute(sb.toString());
			isSchemaCreated = true;
			reader.close();
			in.close();
		}
		return jdbcTemplate;
	}

	protected <T> T getValueOrNull(final ResultSet rs, final T value) throws SQLException {
		if (rs.wasNull()) {
			return null;
		}
		return value;
	}

	protected String truncate(final String value, int limit) {
		if (value.getBytes().length <= limit) {
			return value;
		}

		byte[] utf8 = value.getBytes();
		int n16 = 0;
		int advance = 1;
		int i = 0;
		while (i < limit) {
			advance = 1;
			if ((utf8[i] & 0x80) == 0)
				i += 1;
			else if ((utf8[i] & 0xE0) == 0xC0)
				i += 2;
			else if ((utf8[i] & 0xF0) == 0xE0)
				i += 3;
			else {
				i += 4;
				advance = 2;
			}
			if (i <= limit)
				n16 += advance;
		}
		return value.substring(0, n16);
	}
}
