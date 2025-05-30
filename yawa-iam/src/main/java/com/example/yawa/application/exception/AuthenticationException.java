package com.example.yawa.application.exception;

import org.springframework.http.HttpStatus;

import com.example.springframework.web.LocalizedException;

public abstract class AuthenticationException extends LocalizedException {

  protected AuthenticationException(String code, Object[] args, Throwable cause) {
    super(code, args, cause);
  }

  protected AuthenticationException(String code) {
    super(code);
  }

  protected AuthenticationException(String code, Throwable cause) {
    super(code, cause);
  }

  protected AuthenticationException(String code, Object... args) {
    super(code, args);
  }

  @Override
  public final int getStatusCode() {
    return HttpStatus.UNAUTHORIZED.value();
  }

}
