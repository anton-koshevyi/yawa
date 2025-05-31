package com.example.yawa.iam.user.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.example.yawa.iam.user.model.EmailRequest;

@Getter
@RequiredArgsConstructor
public class EmailRequestCreateEvent {

  private final EmailRequest emailRequest;
  private final String verificationToken;

}
