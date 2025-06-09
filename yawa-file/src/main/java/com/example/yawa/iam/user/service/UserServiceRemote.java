package com.example.yawa.iam.user.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.dto.message.UserGetByIdMessage;
import com.example.yawa.iam.user.exception.NotFoundUserByIdException;
import com.example.yawa.iam.user.model.User;

@Service
public class UserServiceRemote implements UserService {

  private static final int REQUEST_TIMEOUT_SECONDS = 5;

  private static final Logger logger = LoggerFactory.getLogger(UserServiceRemote.class);

  private final AmazonSQSRequester sqsRequester;
  private final ObjectMapper sqsMessageMapper;
  private final String userGetByIdQueueUrl;
  private final String userGetByIdGroupId;

  @Autowired
  public UserServiceRemote(
      AmazonSQSRequester sqsRequester,
      ObjectMapper sqsMessageMapper,
      UserServiceRemoteProperties properties
  ) {
    this.sqsRequester = sqsRequester;
    this.sqsMessageMapper = sqsMessageMapper;
    this.userGetByIdQueueUrl = properties.userGetByIdQueueUrl;
    this.userGetByIdGroupId = properties.userGetByIdGroupId;
  }

  @Override
  public User findById(UUID id) throws NotFoundException {
    try {
      logger.debug("Perform of RPC to find user by ID: {}", id);
      String requestBody = writeMessage(new UserGetByIdMessage(id));
      String requestDeduplicationId = UUID.randomUUID().toString();

      Message response = sqsRequester.sendMessageAndGetResponse(
          SendMessageRequest.builder()
              .queueUrl(userGetByIdQueueUrl)
              .messageBody(requestBody)
              .messageGroupId(userGetByIdGroupId)
              .messageDeduplicationId(requestDeduplicationId)
              .build(),
          REQUEST_TIMEOUT_SECONDS,
          TimeUnit.SECONDS
      );
      String responseBody = response.body();
      logger.debug("Received response: {}", responseBody);

      if ("NOT_FOUND".equalsIgnoreCase(responseBody)) {
        throw new NotFoundUserByIdException(id);
      }

      return readMessage(responseBody, User.class);
    } catch (TimeoutException e) {
      throw new RuntimeException("Timeout of RPC to find user by ID", e);
    }
  }

  private String writeMessage(Object body) {
    try {
      return sqsMessageMapper.writeValueAsString(body);
    } catch (Exception e) {
      throw new RuntimeException("Unable to write SQS message body", e);
    }
  }

  private <T> T readMessage(String body, Class<T> type) {
    try {
      return sqsMessageMapper.readValue(body, type);
    } catch (Exception e) {
      throw new RuntimeException("Unable to read SQS message body", e);
    }
  }


  public static class UserServiceRemoteProperties {

    private final String userGetByIdQueueUrl;
    private final String userGetByIdGroupId;

    public UserServiceRemoteProperties(
        String userGetByIdQueueUrl,
        String userGetByIdGroupId
    ) {
      this.userGetByIdQueueUrl = userGetByIdQueueUrl;
      this.userGetByIdGroupId = userGetByIdGroupId;
    }

  }

}
