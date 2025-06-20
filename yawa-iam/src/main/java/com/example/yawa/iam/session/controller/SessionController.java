package com.example.yawa.iam.session.controller;

import java.util.UUID;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.yawa.application.exception.AuthenticationException;
import com.example.yawa.application.exception.ExpiredException;
import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.session.dto.request.SessionCreateRequest;
import com.example.yawa.iam.session.dto.response.SessionTokenResponse;
import com.example.yawa.iam.session.model.Session;
import com.example.yawa.iam.session.service.SessionService;
import com.example.yawa.iam.session.service.SessionTokenService;
import com.example.yawa.iam.user.model.User;
import com.example.yawa.iam.user.service.UserService;

@RestController
public class SessionController {

  private final SessionService sessionService;
  private final SessionTokenService sessionTokenService;
  private final UserService userService;

  @Autowired
  public SessionController(
      SessionService sessionService,
      SessionTokenService sessionTokenService,
      UserService userService
  ) {
    this.sessionService = sessionService;
    this.sessionTokenService = sessionTokenService;
    this.userService = userService;
  }

  @PostMapping("/sessions")
  @Transactional
  public SessionTokenResponse create(
      @Valid @RequestBody SessionCreateRequest request
  ) throws AuthenticationException {
    String email = request.getEmail();
    String password = request.getPassword();

    User user = userService.getByEmailAndPassword(email, password);
    UUID userId = user.getId();

    Session session = sessionService.create(userId);
    UUID id = session.getId();

    String accessToken = sessionTokenService.generateAccess(id, userId);
    String refreshToken = sessionTokenService.generateRefresh(id);

    return new SessionTokenResponse(accessToken, refreshToken);
  }

  @PutMapping("/sessions")
  @Transactional
  public SessionTokenResponse refresh(
      @RequestHeader("Refresh-Token") String refreshToken
  ) throws ExpiredException, NotFoundException {
    UUID id = sessionTokenService.getSessionId(refreshToken);
    Session session = sessionService.update(id);
    UUID userId = session.getUserId();

    User user = userService.getById(userId);

    String accessToken = sessionTokenService.generateAccess(id, user.getId());
    String rotatedRefreshToken = sessionTokenService.generateRefresh(id);

    return new SessionTokenResponse(accessToken, rotatedRefreshToken);
  }

}
