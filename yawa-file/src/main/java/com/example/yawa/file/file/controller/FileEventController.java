package com.example.yawa.file.file.controller;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.dto.event.FileDeleteEvent;
import com.example.yawa.file.file.dto.event.FileUploadEvent;
import com.example.yawa.file.file.model.File;
import com.example.yawa.file.file.service.EmailService;
import com.example.yawa.iam.user.model.User;
import com.example.yawa.iam.user.service.UserService;

@Component
public class FileEventController implements InitializingBean, DisposableBean {

  private final EmailService emailService;
  private final UserService userService;
  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;
  private final String fileUploadTopicArn;
  private final String fileDeleteTopicArn;
  private final String apiBaseUrl;

  private String fileDeleteSubscriptionArn;

  @Autowired
  public FileEventController(
      EmailService emailService,
      UserService userService,
      SnsClient snsClient,
      ObjectMapper snsMessageMapper,
      FileEventControllerProperties properties
  ) {
    this.emailService = emailService;
    this.userService = userService;
    this.snsClient = snsClient;
    this.snsMessageMapper = snsMessageMapper;
    this.fileUploadTopicArn = properties.fileUploadTopicArn;
    this.fileDeleteTopicArn = properties.fileDeleteTopicArn;
    this.apiBaseUrl = properties.apiBaseUrl;
  }

  @Override
  public void afterPropertiesSet() {
    subscribeToDelete();
  }

  @Override
  public void destroy() {
    unsubscribeFromDelete();
  }

  public void subscribeToDelete() {
    UriComponents deleteEndpoint = UriComponentsBuilder
        .fromHttpUrl(apiBaseUrl)
        .path("/files")
        .build();

    SubscribeResponse response = snsClient
        .subscribe(sr -> sr
            .topicArn(fileDeleteTopicArn)
            .protocol(deleteEndpoint.getScheme())
            .endpoint(deleteEndpoint.toUriString())
            .returnSubscriptionArn(true));
    this.fileDeleteSubscriptionArn = response.subscriptionArn();
  }

  private void unsubscribeFromDelete() {
    snsClient
        .unsubscribe(ur -> ur
            .subscriptionArn(this.fileDeleteSubscriptionArn));
  }

  @EventListener
  public void consumeUpload(FileUploadEvent event) throws NotFoundException {
    File file = event.getFile();
    UUID userId = file.getUserId();

    snsClient
        .publish(pr -> pr
            .topicArn(fileUploadTopicArn)
            .message(writeMessage(file)));

    User user = userService.findById(userId);
    String email = user.getEmail();

    emailService.sendFileUpload(email, file);
  }

  @EventListener
  public void consumeDelete(FileDeleteEvent event) throws NotFoundException {
    File file = event.getFile();
    UUID userId = file.getUserId();

    User user = userService.findById(userId);
    String email = user.getEmail();

    emailService.sendFileDelete(email, file);
  }

  private String writeMessage(Object object) {
    try {
      return snsMessageMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to write object as SNS message", e);
    }
  }


  public static class FileEventControllerProperties {

    private final String fileUploadTopicArn;
    private final String fileDeleteTopicArn;
    private final String apiBaseUrl;

    public FileEventControllerProperties(
        String fileUploadTopicArn,
        String fileDeleteTopicArn,
        String apiBaseUrl
    ) {
      this.fileUploadTopicArn = fileUploadTopicArn;
      this.fileDeleteTopicArn = fileDeleteTopicArn;
      this.apiBaseUrl = apiBaseUrl;
    }

  }

}
