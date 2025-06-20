package com.example.yawa.iam.user.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {

  private UUID id;
  private String actualEmail;
  private String requestedEmail;
  private OffsetDateTime createdAt;

}
