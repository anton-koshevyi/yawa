package com.example.yawa.iam.session.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionRefreshToken {

  private UUID sessionId;
  private String contentCipher;
  private OffsetDateTime createdAt;

}
