package com.example.yawa.iam.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.yawa.iam.user.service.EmailRequestService;

@Component
public class UserScheduleController {

  private final EmailRequestService emailRequestService;

  @Autowired
  public UserScheduleController(EmailRequestService emailRequestService) {
    this.emailRequestService = emailRequestService;
  }

  @Scheduled(fixedRateString = "#{userApplicationProperties.schedule.emailRequestDeletionRate}")
  public void deleteAllExpiredEmailRequests() {
    emailRequestService.deleteAllExpired();
  }

}
