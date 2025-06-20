package com.example.yawa.iam.session.exception;

import com.example.yawa.application.exception.ExpiredException;

public class ExpiredSessionRefreshTokenException extends ExpiredException {

  public ExpiredSessionRefreshTokenException(String refreshToken) {
    super("exception.expired.session.refreshToken", refreshToken);
  }

}
