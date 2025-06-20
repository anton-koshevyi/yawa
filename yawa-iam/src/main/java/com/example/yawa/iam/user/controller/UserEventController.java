package com.example.yawa.iam.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.yawa.iam.user.dto.event.EmailRequestCreateEvent;
import com.example.yawa.iam.user.dto.event.UserCreateEvent;
import com.example.yawa.iam.user.service.EmailService;

@Component
public class UserEventController {

  private final EmailService emailService;

  @Autowired
  public UserEventController(EmailService emailService) {
    this.emailService = emailService;
  }

  @EventListener
  public void consumeSignUpInit(EmailRequestCreateEvent event) {
    String email = event.getEmailRequest().getRequestedEmail();
    String verificationToken = event.getVerificationToken();

    emailService.sendSignUpInit(email, verificationToken);
  }

  @EventListener
  public void consumeSignUpComplete(UserCreateEvent event) {
    String email = event.getUser().getEmail();

    emailService.sendSignUpComplete(email);
  }

}
