package com.example.yawa.iam.session.exception;

import com.example.yawa.application.exception.ExpiredException;

public class ExpiredSessionAccessTokenException extends ExpiredException {

  public ExpiredSessionAccessTokenException(String accessToken, Throwable cause) {
    super("exception.expired.session.accessToken", new Object[]{accessToken}, cause);
  }

}
