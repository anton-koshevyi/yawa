package com.example.yawa.file.file.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;

import com.example.yawa.file.file.model.File;

@Service
public class EmailServiceRemote implements EmailService, InitializingBean, DisposableBean {

  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;
  private final String emailTopicArn;
  private final String emailLambdaArn;

  private String emailSubscriptionArn;

  @Autowired
  public EmailServiceRemote(
      SnsClient snsClient,
      ObjectMapper snsMessageMapper,
      EmailServiceRemoteProperties properties
  ) {
    this.snsClient = snsClient;
    this.snsMessageMapper = snsMessageMapper;
    this.emailTopicArn = properties.emailTopicArn;
    this.emailLambdaArn = properties.emailLambdaArn;
  }

  @Override
  public void sendFileUpload(String email, File file) {
    String body = ""
        + "File uploaded: " + file.getName()
        + "; URL: " + file.getUrl()
        + "; Expires at: " + file.getUrlExpiredAt();

    Map<String, String> input = new LinkedHashMap<>();
    input.put("recipient", email);
    input.put("subject", "File upload");
    input.put("body", body);
    publish(input);
  }

  @Override
  public void sendFileDelete(String email, File file) {
    String body = "File has been deleted: " + file.getName();

    Map<String, String> input = new LinkedHashMap<>();
    input.put("recipient", email);
    input.put("subject", "File deletion");
    input.put("body", body);
    publish(input);
  }

  @Override
  public void afterPropertiesSet() {
    this.emailSubscriptionArn = snsClient
        .subscribe(sr -> sr
            .topicArn(emailTopicArn)
            .protocol("lambda")
            .endpoint(emailLambdaArn)
            .returnSubscriptionArn(true))
        .subscriptionArn();
  }

  @Override
  public void destroy() {
    snsClient
        .unsubscribe(ur -> ur
            .subscriptionArn(emailSubscriptionArn));
  }

  private void publish(Map<String, String> input) {
    snsClient
        .publish(pr -> pr
            .topicArn(emailTopicArn)
            .message(writeMessage(input)));
  }

  private String writeMessage(Object object) {
    try {
      return snsMessageMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to write object as SNS message", e);
    }
  }


  public static class EmailServiceRemoteProperties {

    private final String emailTopicArn;
    private final String emailLambdaArn;

    public EmailServiceRemoteProperties(
        String emailTopicArn,
        String emailLambdaArn
    ) {
      this.emailTopicArn = emailTopicArn;
      this.emailLambdaArn = emailLambdaArn;
    }

  }

}
