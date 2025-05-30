package com.example.yawa.application.exception;

import org.springframework.http.HttpStatus;

import com.example.springframework.web.LocalizedException;

public abstract class ExpiredException extends LocalizedException {

  protected ExpiredException(String code, Object[] args, Throwable cause) {
    super(code, args, cause);
  }

  protected ExpiredException(String code) {
    super(code);
  }

  protected ExpiredException(String code, Throwable cause) {
    super(code, cause);
  }

  protected ExpiredException(String code, Object... args) {
    super(code, args);
  }

  @Override
  public final int getStatusCode() {
    return HttpStatus.GONE.value();
  }

}
