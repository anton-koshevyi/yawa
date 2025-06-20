package com.example.yawa.iam.user.service;

import java.time.Clock;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.ExpiredException;
import com.example.yawa.iam.user.config.ApplicationProperties;
import com.example.yawa.iam.user.exception.ExpiredEmailRequestVerificationTokenException;

@Service
public class EmailRequestTokenService {

  private final TokenService tokenService;
  private final Clock clock;
  private final Integer verificationTokenExpirationSeconds;

  @Autowired
  public EmailRequestTokenService(
      TokenService tokenService,
      Clock clock,
      ApplicationProperties properties
  ) {
    this.tokenService = tokenService;
    this.clock = clock;
    this.verificationTokenExpirationSeconds = properties.getToken()
        .getEmailRequestVerificationExpirationSeconds();
  }

  public String generateVerification(UUID emailRequestId) {
    String info = constructVerificationInfo(emailRequestId);
    Token token = tokenService.allocateToken(info);

    return token.getKey();
  }

  public UUID getEmailRequestId(String verificationToken) throws ExpiredException {
    try {
      return Optional.ofNullable(tokenService.verifyToken(verificationToken))
          .filter(this::isNotExpiredVerification)
          .map(this::parseVerificationInfo)
          .orElseThrow(NoSuchElementException::new);
    } catch (Exception e) {
      throw new ExpiredEmailRequestVerificationTokenException(verificationToken);
    }
  }

  private String constructVerificationInfo(UUID sessionId) {
    return String.valueOf(sessionId);
  }

  private UUID parseVerificationInfo(Token token) {
    return UUID.fromString(token.getExtendedInformation());
  }

  private boolean isNotExpiredVerification(Token token) {
    Instant expiredAt = Instant.now(clock).minusSeconds(verificationTokenExpirationSeconds);
    Instant createdAt = Instant.ofEpochMilli(token.getKeyCreationTime());

    return expiredAt.isBefore(createdAt);
  }

}
