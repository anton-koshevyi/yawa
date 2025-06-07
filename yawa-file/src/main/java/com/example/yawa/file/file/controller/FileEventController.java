package com.example.yawa.file.file.controller;

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

import com.example.yawa.file.file.dto.event.FileUploadEvent;
import com.example.yawa.file.file.model.File;

@Component
public class FileEventController implements InitializingBean, DisposableBean {

  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;
  private final String fileUploadTopicArn;
  private final String fileDeleteTopicArn;
  private final String apiBaseUrl;

  private String fileDeleteSubscriptionArn;

  @Autowired
  public FileEventController(
      SnsClient snsClient,
      ObjectMapper snsMessageMapper,
      FileEventControllerProperties properties
  ) {
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
  public void consumeUpload(FileUploadEvent event) {
    File file = event.getFile();

    snsClient
        .publish(pr -> pr
            .topicArn(fileUploadTopicArn)
            .message(writeMessage(file)));
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
