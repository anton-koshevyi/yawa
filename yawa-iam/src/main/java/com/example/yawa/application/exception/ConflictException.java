package com.example.yawa.application.exception;

import org.springframework.http.HttpStatus;

import com.example.springframework.web.LocalizedException;

public abstract class ConflictException extends LocalizedException {

  protected ConflictException(String code, Object[] args, Throwable cause) {
    super(code, args, cause);
  }

  protected ConflictException(String code) {
    super(code);
  }

  protected ConflictException(String code, Throwable cause) {
    super(code, cause);
  }

  protected ConflictException(String code, Object... args) {
    super(code, args);
  }

  @Override
  public final int getStatusCode() {
    return HttpStatus.CONFLICT.value();
  }

}
