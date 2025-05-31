package com.example.yawa.iam.user.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
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

import com.example.yawa.iam.user.model.EmailRequest;

@Repository
public class EmailRequestRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert jdbcInsert;

  @Autowired
  public EmailRequestRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("email_requests")
        .usingColumns("actual_email", "requested_email", "created_at")
        .usingGeneratedKeyColumns("id");
  }

  public UUID create(EmailRequest model) {
    SqlParameterSource params = Assembler.disassemble(model);

    KeyHolder keyHolder = jdbcInsert.executeAndReturnKeyHolder(params);

    return getFirstKey(keyHolder, UUID.class);
  }

  public void deleteAllByRequestedEmail(String requestedEmail) {
    String sql = ""
        + " delete from email_requests"
        + "  where requested_email = :requestedEmail";
    SqlParameterSource params = new MapSqlParameterSource("requestedEmail", requestedEmail);

    jdbcTemplate.update(sql, params);
  }

  public void deleteAllByCreatedAtBefore(OffsetDateTime createdAt) {
    String sql = ""
        + " delete from email_requests"
        + "  where created_at < :createdAt";
    SqlParameterSource params = new MapSqlParameterSource("createdAt", createdAt);

    jdbcTemplate.update(sql, params);
  }

  public Optional<EmailRequest> findById(UUID id) {
    String sql = ""
        + " select *"
        + "   from email_requests"
        + "  where id = :id";
    SqlParameterSource params = new MapSqlParameterSource("id", id);

    try {
      EmailRequest model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

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

    private static final RowMapper<EmailRequest> rowMapper =
        new BeanPropertyRowMapper<>(EmailRequest.class, true);

    static EmailRequest assemble(ResultSet rs, int rowNum) throws SQLException {
      return rowMapper.mapRow(rs, rowNum);
    }

    static SqlParameterSource disassemble(EmailRequest model) {
      AbstractSqlParameterSource params = new BeanPropertySqlParameterSource(model);
      params.registerSqlType("createdAt", Types.TIMESTAMP_WITH_TIMEZONE);
      return params;
    }

  }

}
