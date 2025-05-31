package com.example.yawa.iam.user.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestCreateRequest {

  @NotNull
  @Email
  private String email;

}
