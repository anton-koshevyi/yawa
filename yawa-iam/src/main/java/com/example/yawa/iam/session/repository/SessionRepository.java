package com.example.yawa.iam.session.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.yawa.iam.session.model.Session;

@Repository
public class SessionRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert jdbcInsert;

  @Autowired
  public SessionRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("sessions")
        .usingColumns("user_id", "accessed_at")
        .usingGeneratedKeyColumns("id");
  }

  public UUID create(Session model) {
    SqlParameterSource params = Assembler.disassemble(model);

    KeyHolder keyHolder = jdbcInsert.executeAndReturnKeyHolder(params);

    return getFirstKey(keyHolder, UUID.class);
  }

  public void update(Session model) {
    String sql = ""
        + " update sessions"
        + "    set accessed_at = :accessedAt"
        + "      , user_id = :userId"
        + "  where id = :id";
    SqlParameterSource params = Assembler.disassemble(model);

    jdbcTemplate.update(sql, params);
  }

  public void deleteAllByIds(UUID[] ids) {
    String sql = ""
        + " delete from sessions"
        + "  where id = :id";
    SqlParameterSource[] params = Arrays.stream(ids)
        .map(id -> new MapSqlParameterSource("id", id))
        .toArray(SqlParameterSource[]::new);

    jdbcTemplate.batchUpdate(sql, params);
  }

  public Optional<Session> findById(UUID id) {
    String sql = ""
        + " select *"
        + "   from sessions"
        + "  where id = :id";
    SqlParameterSource params = new MapSqlParameterSource("id", id);

    try {
      Session model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

      return Optional.ofNullable(model);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  private static <T> T getFirstKey(KeyHolder keyHolder, Class<T> type) {
    Map<String, Object> keys = keyHolder.getKeys();
    Iterator<Object> keyIterator;

    if (keys == null || !(keyIterator = keys.values().iterator()).hasNext()) {
      throw new IncorrectResultSetColumnCountException(1, 0);
    }

    return type.cast(keyIterator.next());
  }


  private static class Assembler {

    private static final RowMapper<Session> rowMapper =
        new BeanPropertyRowMapper<>(Session.class, true);

    static Session assemble(ResultSet rs, int rowNum) throws SQLException {
      return rowMapper.mapRow(rs, rowNum);
    }

    static SqlParameterSource disassemble(Session model) {
      AbstractSqlParameterSource params = new BeanPropertySqlParameterSource(model);
      params.registerSqlType("accessedAt", Types.TIMESTAMP_WITH_TIMEZONE);
      return params;
    }

  }

}
