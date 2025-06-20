package com.example.yawa.iam.user.exception;

import com.example.yawa.application.exception.ExpiredException;

public class ExpiredEmailRequestVerificationTokenException extends ExpiredException {

  public ExpiredEmailRequestVerificationTokenException(String verificationToken) {
    super("exception.expired.emailRequest.verificationToken", verificationToken);
  }

}
