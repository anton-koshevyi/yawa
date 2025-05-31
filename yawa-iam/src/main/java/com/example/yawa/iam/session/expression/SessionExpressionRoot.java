package com.example.yawa.iam.session.expression;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.Authentication;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;
import com.example.yawa.iam.session.dto.SessionPrincipal;

public class SessionExpressionRoot extends SecurityExpressionRootExtended {

  public SessionExpressionRoot(Authentication auth) {
    super(auth);
  }

  @Override
  public boolean isUser(UUID userId) {
    return withPrincipal(principal -> principal.getUserId().equals(userId), false);
  }

  @Override
  public UUID getUserId() {
    return withPrincipal(SessionPrincipal::getUserId, null);
  }

  @Override
  public boolean isSession(UUID sessionId) {
    return withPrincipal(principal -> principal.getSessionId().equals(sessionId), false);
  }

  @Override
  public UUID getSessionId() {
    return withPrincipal(SessionPrincipal::getSessionId, null);
  }

  private <R> R withPrincipal(Function<SessionPrincipal, R> action, R defaultValue) {
    return withAuthentication(Authentication::getPrincipal, action, defaultValue);
  }

  private <T, R> R withAuthentication(
      Function<Authentication, Object> extractor,
      Function<T, R> action,
      R defaultValue
  ) {
    try {
      T object = (T) extractor.apply(authentication);
      return action.apply(object);
    } catch (Exception e) {
      return defaultValue;
    }
  }

}
