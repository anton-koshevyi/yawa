package com.example.yawa.iam.user.exception;

import com.example.yawa.application.exception.ConflictException;

public class ConflictUserEmailException extends ConflictException {

  public ConflictUserEmailException(String email) {
    super("exception.conflict.user.email", email);
  }

}
