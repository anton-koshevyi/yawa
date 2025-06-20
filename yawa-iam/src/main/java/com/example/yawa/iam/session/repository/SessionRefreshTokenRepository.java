package com.example.yawa.iam.session.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.yawa.iam.session.model.SessionRefreshToken;

@Repository
public class SessionRefreshTokenRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert jdbcInsert;
  private final TransactionTemplate transactionTemplate;

  @Autowired
  public SessionRefreshTokenRepository(
      JdbcTemplate jdbcTemplate,
      TransactionTemplate transactionTemplate
  ) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("session_refresh_tokens")
        .usingColumns("session_id", "content_cipher", "created_at");
    this.transactionTemplate = transactionTemplate;
  }

  public void create(SessionRefreshToken model) {
    SqlParameterSource params = Assembler.disassemble(model);

    jdbcInsert.execute(params);
  }

  public void update(SessionRefreshToken model) {
    String sql = ""
        + " update session_refresh_tokens"
        + "    set content_cipher = :contentCipher"
        + "      , created_at = :createdAt"
        + "  where session_id = :sessionId";
    SqlParameterSource params = Assembler.disassemble(model);

    jdbcTemplate.update(sql, params);
  }

  public SessionRefreshToken[] deleteAllByCreatedAtBefore(OffsetDateTime createdAt) {
    return transactionTemplate.execute(status -> {
      String findSql = ""
          + " select *"
          + "   from session_refresh_tokens"
          + "  where created_at < :createdAt";
      SqlParameterSource findParams = new MapSqlParameterSource("createdAt", createdAt);

      List<SessionRefreshToken> models =
          jdbcTemplate.query(findSql, findParams, Assembler::assemble);

      String deleteSql = ""
          + " delete from session_refresh_tokens"
          + "  where session_id = :sessionId";
      SqlParameterSource[] deleteParams = models.stream()
          .map(Assembler::disassemble)
          .toArray(SqlParameterSource[]::new);

      jdbcTemplate.batchUpdate(deleteSql, deleteParams);

      return models.toArray(new SessionRefreshToken[0]);
    });
  }

  public Optional<SessionRefreshToken> findBySessionId(UUID sessionId) {
    String sql = ""
        + " select *"
        + "   from session_refresh_tokens"
        + "  where session_id = :sessionId";
    SqlParameterSource params = new MapSqlParameterSource("sessionId", sessionId);

    try {
      SessionRefreshToken model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

      return Optional.ofNullable(model);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<SessionRefreshToken> findByContentCipher(String contentCipher) {
    String sql = ""
        + " select *"
        + "   from session_refresh_tokens"
        + "  where content_cipher = :contentCipher";
    SqlParameterSource params = new MapSqlParameterSource("contentCipher", contentCipher);

    try {
      SessionRefreshToken model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

      return Optional.ofNullable(model);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public boolean existsByContentCipher(String contentCipher) {
    return this.findByContentCipher(contentCipher).isPresent();
  }


  private static class Assembler {

    private static final RowMapper<SessionRefreshToken> rowMapper =
        new BeanPropertyRowMapper<>(SessionRefreshToken.class, true);

    static SessionRefreshToken assemble(ResultSet rs, int rowNum) throws SQLException {
      return rowMapper.mapRow(rs, rowNum);
    }

    static SqlParameterSource disassemble(SessionRefreshToken model) {
      AbstractSqlParameterSource params = new BeanPropertySqlParameterSource(model);
      params.registerSqlType("createdAt", Types.TIMESTAMP_WITH_TIMEZONE);
      return params;
    }

  }

}
