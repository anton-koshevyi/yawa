package com.example.yawa.iam.session.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Session {

  private UUID id;
  private UUID userId;
  private OffsetDateTime accessedAt;

}
