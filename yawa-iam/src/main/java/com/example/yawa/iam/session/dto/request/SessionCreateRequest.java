package com.example.yawa.iam.session.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionCreateRequest {

  @NotNull
  @Email
  private String email;

  @NotNull
  private String password;

}
