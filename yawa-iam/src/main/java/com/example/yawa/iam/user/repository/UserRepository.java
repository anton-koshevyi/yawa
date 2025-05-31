package com.example.yawa.iam.user.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

import com.example.yawa.iam.user.model.User;

@Repository
public class UserRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert jdbcInsert;

  @Autowired
  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("users")
        .usingColumns("email", "password_hash")
        .usingGeneratedKeyColumns("id");
  }

  public UUID create(User model) {
    SqlParameterSource params = Assembler.disassemble(model);

    KeyHolder keyHolder = jdbcInsert.executeAndReturnKeyHolder(params);

    return getFirstKey(keyHolder, UUID.class);
  }

  public Optional<User> findById(UUID id) {
    String sql = ""
        + " select *"
        + "   from users"
        + "  where id = :id";
    SqlParameterSource params = new MapSqlParameterSource("id", id);

    try {
      User model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

      return Optional.ofNullable(model);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<User> findByEmail(String email) {
    String sql = ""
        + " select *"
        + "   from users"
        + "  where email = :email";
    SqlParameterSource params = new MapSqlParameterSource("email", email);

    try {
      User model = jdbcTemplate.queryForObject(sql, params, Assembler::assemble);

      return Optional.ofNullable(model);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public boolean existsByEmail(String email) {
    return this.findByEmail(email).isPresent();
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

    private static final RowMapper<User> rowMapper =
        new BeanPropertyRowMapper<>(User.class, true);

    static User assemble(ResultSet rs, int rowNum) throws SQLException {
      return rowMapper.mapRow(rs, rowNum);
    }

    static SqlParameterSource disassemble(User model) {
      AbstractSqlParameterSource params = new BeanPropertySqlParameterSource(model);
      params.registerSqlType("createdAt", Types.TIMESTAMP_WITH_TIMEZONE);
      return params;
    }

  }

}
