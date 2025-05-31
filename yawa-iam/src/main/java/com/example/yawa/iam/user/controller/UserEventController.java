package com.example.yawa.iam.user.controller;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.yawa.iam.user.dto.event.EmailRequestCreateEvent;
import com.example.yawa.iam.user.dto.event.UserCreateEvent;

@Component
public class UserEventController {

  @EventListener
  public void consumeSignUpInit(EmailRequestCreateEvent event) {
    // TODO: Publish event to send email
  }

  @EventListener
  public void consumeSignUpComplete(UserCreateEvent event) {
    // TODO: Publish event to send email
  }

}
