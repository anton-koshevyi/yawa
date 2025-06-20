package com.example.yawa.iam.session.service;

import java.security.Key;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Cipher;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.ExpiredException;
import com.example.yawa.iam.session.config.ApplicationProperties;
import com.example.yawa.iam.session.config.ApplicationProperties.TokenProperties;
import com.example.yawa.iam.session.exception.ExpiredSessionRefreshTokenException;
import com.example.yawa.iam.session.model.SessionRefreshToken;
import com.example.yawa.iam.session.repository.KeyRepository;
import com.example.yawa.iam.session.repository.SessionRefreshTokenRepository;
import com.example.yawa.iam.session.util.SessionAccessTokenClaims;

@Service
public class SessionTokenService {

  private final SessionRefreshTokenRepository sessionRefreshTokenRepository;
  private final KeyRepository keyRepository;
  private final Clock clock;
  private final Cipher sessionRefreshTokenCipher;
  private final Integer accessTokenExpirationSeconds;
  private final Integer refreshTokenExpirationSeconds;

  @Autowired
  public SessionTokenService(
      SessionRefreshTokenRepository sessionRefreshTokenRepository,
      KeyRepository keyRepository,
      Clock clock,
      Cipher sessionRefreshTokenCipher,
      ApplicationProperties properties
  ) {
    this.sessionRefreshTokenRepository = sessionRefreshTokenRepository;
    this.keyRepository = keyRepository;
    this.clock = clock;
    this.sessionRefreshTokenCipher = sessionRefreshTokenCipher;

    TokenProperties tokenProperties = properties.getToken();
    this.accessTokenExpirationSeconds = tokenProperties.getSessionAccessExpirationSeconds();
    this.refreshTokenExpirationSeconds = tokenProperties.getSessionRefreshExpirationSeconds();
  }

  public String generateAccess(UUID sessionId, UUID userId) {
    Key signingKey = getAccessSigningKey();
    OffsetDateTime createdAt = OffsetDateTime.now(clock);
    OffsetDateTime expiredAt = createdAt.plusSeconds(accessTokenExpirationSeconds);

    return Jwts.builder()
        .setSubject(userId.toString())
        .setIssuedAt(Date.from(createdAt.toInstant()))
        .setExpiration(Date.from(expiredAt.toInstant()))
        .claim(SessionAccessTokenClaims.SESSION_ID, sessionId)
        .signWith(SignatureAlgorithm.HS256, signingKey)
        .compact();
  }

  public String generateRefresh(UUID sessionId) {
    String content;
    String contentCipher;

    do {
      content = generateRefreshToken();
      contentCipher = cipherRefreshToken(content);
    } while (sessionRefreshTokenRepository.existsByContentCipher(contentCipher));

    Optional<SessionRefreshToken> tokenOptional =
        sessionRefreshTokenRepository.findBySessionId(sessionId);
    OffsetDateTime createdAt = OffsetDateTime.now(clock);

    if (tokenOptional.isPresent()) {
      SessionRefreshToken token = tokenOptional.get();
      token.setContentCipher(contentCipher);
      token.setCreatedAt(createdAt);
      sessionRefreshTokenRepository.update(token);
    } else {
      SessionRefreshToken token = new SessionRefreshToken();
      token.setSessionId(sessionId);
      token.setContentCipher(contentCipher);
      token.setCreatedAt(createdAt);
      sessionRefreshTokenRepository.create(token);
    }

    return content;
  }

  public UUID[] deleteAllExpiredRefresh() {
    OffsetDateTime expiredAt = OffsetDateTime.now(clock)
        .minusSeconds(refreshTokenExpirationSeconds);

    return Arrays.stream(sessionRefreshTokenRepository
        .deleteAllByCreatedAtBefore(expiredAt))
        .map(SessionRefreshToken::getSessionId)
        .toArray(UUID[]::new);
  }

  public UUID getSessionId(String refreshToken) throws ExpiredException {
    String tokenCipher = cipherRefreshToken(refreshToken);
    OffsetDateTime expiredAt = OffsetDateTime.now(clock)
        .minusSeconds(refreshTokenExpirationSeconds);

    return sessionRefreshTokenRepository.findByContentCipher(tokenCipher)
        .filter(token -> expiredAt.isBefore(token.getCreatedAt()))
        .map(SessionRefreshToken::getSessionId)
        .orElseThrow(() -> new ExpiredSessionRefreshTokenException(refreshToken));
  }

  private Key getAccessSigningKey() {
    return keyRepository.findSessionAccess()
        .orElseThrow(() -> new NoSuchElementException(
            "No signing key for session access token"));
  }

  private String cipherRefreshToken(String token) {
    Key key = keyRepository.findSessionRefresh()
        .orElseThrow(() -> new NoSuchElementException(
            "No key for session refresh token"));

    try {
      sessionRefreshTokenCipher.init(Cipher.ENCRYPT_MODE, key);

      byte[] cipheredBytes = sessionRefreshTokenCipher.doFinal(token.getBytes());

      return Hex.encodeHexString(cipheredBytes);
    } catch (Exception e) {
      throw new RuntimeException("Unable to cipher session refresh token", e);
    }
  }

  private static String generateRefreshToken() {
    return UUID.randomUUID()
        .toString()
        .replace("-", "");
  }

}
