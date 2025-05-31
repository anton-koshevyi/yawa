package com.example.yawa.iam.user.controller;

import java.util.UUID;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.yawa.application.exception.ConflictException;
import com.example.yawa.application.exception.ExpiredException;
import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.dto.event.EmailRequestCreateEvent;
import com.example.yawa.iam.user.dto.event.UserCreateEvent;
import com.example.yawa.iam.user.dto.request.EmailRequestCreateRequest;
import com.example.yawa.iam.user.dto.request.UserCreateRequest;
import com.example.yawa.iam.user.model.EmailRequest;
import com.example.yawa.iam.user.model.User;
import com.example.yawa.iam.user.service.EmailRequestService;
import com.example.yawa.iam.user.service.EmailRequestTokenService;
import com.example.yawa.iam.user.service.UserService;

@RestController
public class UserController {

  private final UserService userService;
  private final EmailRequestService emailRequestService;
  private final EmailRequestTokenService emailRequestTokenService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public UserController(
      UserService userService,
      EmailRequestService emailRequestService,
      EmailRequestTokenService emailRequestTokenService,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.userService = userService;
    this.emailRequestService = emailRequestService;
    this.emailRequestTokenService = emailRequestTokenService;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @PostMapping("/users/emails")
  @Transactional
  public void createEmailRequest(@Valid @RequestBody EmailRequestCreateRequest request) {
    String requestedEmail = request.getEmail();

    EmailRequest emailRequest = emailRequestService.create(null, requestedEmail);
    UUID emailRequestId = emailRequest.getId();

    String verificationToken = emailRequestTokenService.generateVerification(emailRequestId);

    Object event = new EmailRequestCreateEvent(emailRequest, verificationToken);
    applicationEventPublisher.publishEvent(event);
  }

  @PutMapping("/users/emails")
  @Transactional
  public void createUser(
      @Valid @RequestBody UserCreateRequest request
  ) throws ExpiredException, NotFoundException, ConflictException {
    String verificationToken = request.getVerificationToken();
    String password = request.getPassword();

    UUID emailRequestId = emailRequestTokenService.getEmailRequestId(verificationToken);
    EmailRequest emailRequest = emailRequestService.getById(emailRequestId);
    String requestedEmail = emailRequest.getRequestedEmail();

    User user = userService.create(requestedEmail, password);
    emailRequestService.deleteAllByRequestedEmail(requestedEmail);

    Object event = new UserCreateEvent(emailRequest, user);
    applicationEventPublisher.publishEvent(event);
  }

}
