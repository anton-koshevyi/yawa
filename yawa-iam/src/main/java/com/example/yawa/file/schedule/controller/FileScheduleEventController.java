package com.example.yawa.file.schedule.controller;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

@Component
public class FileScheduleEventController implements InitializingBean, DisposableBean {

  private final SnsClient snsClient;
  private final String fileUploadTopicArn;
  private final String apiBaseUrl;

  private String subscriptionArnScheduleDeletion;

  @Autowired
  public FileScheduleEventController(
      SnsClient snsClient,
      FileScheduleEventControllerProperties properties
  ) {
    this.snsClient = snsClient;
    this.fileUploadTopicArn = properties.fileUploadTopicArn;
    this.apiBaseUrl = properties.apiBaseUrl;
  }

  @Override
  public void afterPropertiesSet() {
    subscribeToScheduleDeletion();
  }

  @Override
  public void destroy() {
    unsubscribeFromScheduleDeletion();
  }

  public void subscribeToScheduleDeletion() {
    UriComponents scheduleDeletionEndpoint = UriComponentsBuilder
        .fromHttpUrl(apiBaseUrl)
        .path("/file-schedules/deletions")
        .build();

    SubscribeResponse response = snsClient
        .subscribe(sr -> sr
            .topicArn(fileUploadTopicArn)
            .protocol(scheduleDeletionEndpoint.getScheme())
            .endpoint(scheduleDeletionEndpoint.toUriString())
            .returnSubscriptionArn(true));
    this.subscriptionArnScheduleDeletion = response.subscriptionArn();
  }

  private void unsubscribeFromScheduleDeletion() {
    snsClient
        .unsubscribe(ur -> ur
            .subscriptionArn(this.subscriptionArnScheduleDeletion));
  }


  public static class FileScheduleEventControllerProperties {

    private final String fileUploadTopicArn;
    private final String apiBaseUrl;

    public FileScheduleEventControllerProperties(
        String fileUploadTopicArn,
        String apiBaseUrl
    ) {
      this.fileUploadTopicArn = fileUploadTopicArn;
      this.apiBaseUrl = apiBaseUrl;
    }

  }

}
