package com.example.yawa.iam.user.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.example.yawa.iam.user.model.EmailRequest;
import com.example.yawa.iam.user.model.User;

@Getter
@RequiredArgsConstructor
public class UserCreateEvent {

  private final EmailRequest emailRequest;
  private final User user;

}
