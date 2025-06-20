package com.example.yawa.application.exception;

import org.springframework.http.HttpStatus;

import com.example.springframework.web.LocalizedException;

public abstract class NotFoundException extends LocalizedException {

  protected NotFoundException(String code, Object[] args, Throwable cause) {
    super(code, args, cause);
  }

  protected NotFoundException(String code) {
    super(code);
  }

  protected NotFoundException(String code, Throwable cause) {
    super(code, cause);
  }

  protected NotFoundException(String code, Object... args) {
    super(code, args);
  }

  @Override
  public final int getStatusCode() {
    return HttpStatus.NOT_FOUND.value();
  }

}
