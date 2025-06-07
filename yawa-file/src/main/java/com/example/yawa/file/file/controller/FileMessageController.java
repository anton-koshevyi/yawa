package com.example.yawa.file.file.controller;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sns.SnsClient;

import com.example.yawa.application.exception.NotFoundException;

@RestController
public class FileMessageController {

  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;

  @Autowired
  public FileMessageController(
      SnsClient snsClient,
      ObjectMapper snsMessageMapper
  ) {
    this.snsClient = snsClient;
    this.snsMessageMapper = snsMessageMapper;
  }

  @PostMapping(
      path = "/files",
      headers = "x-amz-sns-message-type=SubscriptionConfirmation",
      consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public void confirmSubscriptionToDelete(@RequestBody String requestBodyText) {
    Map<String, String> requestBody = readMessage(requestBodyText);
    String topicArn = requestBody.get("TopicArn");
    String token = requestBody.get("Token");

    snsClient
        .confirmSubscription(csr -> csr
            .topicArn(topicArn)
            .token(token));
  }

  @PostMapping(
      path = "/files",
      headers = "x-amz-sns-message-type=Notification",
      consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public void delete(@RequestBody String requestBodyText) throws NotFoundException {
    Map<String, String> requestBody = readMessage(requestBodyText);
    String fileId = requestBody.get("Message");

    // TODO: Delete file
  }

  private Map<String, String> readMessage(String message) {
    try {
      return snsMessageMapper.readValue(message, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Unable to read SNS message: " + message, e);
    }
  }

}
