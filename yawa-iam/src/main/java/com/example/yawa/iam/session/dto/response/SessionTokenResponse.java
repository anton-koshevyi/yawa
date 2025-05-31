package com.example.yawa.iam.session.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionTokenResponse {

  private final String accessToken;
  private final String refreshToken;

}
