package com.example.yawa.iam.session.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString(includeFieldNames = false, of = "sessionId")
public class SessionPrincipal {

  private final UUID sessionId;
  private final UUID userId;

}
