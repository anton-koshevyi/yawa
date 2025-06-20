package com.example.yawa.iam.user.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

  @NotNull
  private String verificationToken;

  @NotNull
  private String password;

}
