package com.example.yawa.file.schedule.controller;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sns.SnsClient;

import com.example.yawa.file.file.model.File;

@RestController
public class FileScheduleMessageController {

  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;

  @Autowired
  public FileScheduleMessageController(
      SnsClient snsClient,
      ObjectMapper snsMessageMapper
  ) {
    this.snsClient = snsClient;
    this.snsMessageMapper = snsMessageMapper;
  }

  @PostMapping(
      path = "/file-schedules/deletions",
      headers = "x-amz-sns-message-type=SubscriptionConfirmation",
      consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public void confirmSubscriptionToScheduleDeletion(@RequestBody String requestBodyText) {
    Map<String, String> requestBody = readMessage(requestBodyText);
    String topicArn = requestBody.get("TopicArn");
    String token = requestBody.get("Token");

    snsClient
        .confirmSubscription(csr -> csr
            .topicArn(topicArn)
            .token(token));
  }

  @PostMapping(
      path = "/file-schedules/deletions",
      headers = "x-amz-sns-message-type=Notification",
      consumes = MediaType.TEXT_PLAIN_VALUE
  )
  public void scheduleDeletion(@RequestBody String requestBodyText) {
    Map<String, String> requestBody = readMessage(requestBodyText);
    String fileJson = requestBody.get("Message");

    File file = readMessage(fileJson, File.class);
    String fileId = file.getId();

    // TODO: Schedule file delete
  }

  private Map<String, String> readMessage(String message) {
    try {
      return snsMessageMapper.readValue(message, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      throw new RuntimeException("Unable to read SNS message: " + message, e);
    }
  }

  private <T> T readMessage(String message, Class<T> type) {
    try {
      return snsMessageMapper.readValue(message, type);
    } catch (Exception e) {
      throw new RuntimeException("Unable to read SNS message: " + message, e);
    }
  }

}
