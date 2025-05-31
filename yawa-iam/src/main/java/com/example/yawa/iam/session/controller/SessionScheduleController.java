package com.example.yawa.iam.session.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.yawa.iam.session.service.SessionService;
import com.example.yawa.iam.session.service.SessionTokenService;

@Component
public class SessionScheduleController {

  private final SessionService sessionService;
  private final SessionTokenService sessionTokenService;

  @Autowired
  public SessionScheduleController(
      SessionService sessionService,
      SessionTokenService sessionTokenService
  ) {
    this.sessionService = sessionService;
    this.sessionTokenService = sessionTokenService;
  }

  @Scheduled(fixedRateString =
      "#{sessionApplicationProperties.schedule.sessionRefreshTokenDeletionRate}")
  public void deleteAllExpiredSessions() {
    UUID[] sessionIds = sessionTokenService.deleteAllExpiredRefresh();
    sessionService.deleteAllByIds(sessionIds);
  }

}
