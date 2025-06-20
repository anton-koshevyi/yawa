package com.example.yawa.iam.user.exception;

import com.example.yawa.application.exception.AuthenticationException;

public class AuthenticationUserByEmailAndPasswordException extends AuthenticationException {

  public AuthenticationUserByEmailAndPasswordException(String email, String password) {
    super("exception.authentication.user.byEmailAndPassword", email, password);
  }

}
