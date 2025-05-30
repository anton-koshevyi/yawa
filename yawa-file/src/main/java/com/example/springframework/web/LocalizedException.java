package com.example.springframework.web;

import org.springframework.context.MessageSourceResolvable;

public abstract class LocalizedException
    extends Exception
    implements MessageSourceResolvable {

  private final String code;
  private final Object[] args;

  protected LocalizedException(String code, Object[] args, Throwable cause) {
    super(cause);
    this.code = code;
    this.args = args;
  }

  protected LocalizedException(String code) {
    this(code, null, null);
  }

  protected LocalizedException(String code, Throwable cause) {
    this(code, null, cause);
  }

  protected LocalizedException(String code, Object... args) {
    this(code, args, null);
  }

  public abstract int getStatusCode();

  @Override
  public final String[] getCodes() {
    return new String[]{code};
  }

  @Override
  public final Object[] getArguments() {
    return args;
  }

  @Override
  public final String getDefaultMessage() {
    return null;
  }

}
