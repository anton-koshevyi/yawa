package com.example.yawa.iam.user.controller;

import java.util.UUID;

import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.util.SQSMessageConsumer;
import com.amazonaws.services.sqs.util.SQSMessageConsumerBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.dto.message.UserGetByIdMessage;
import com.example.yawa.iam.user.model.User;
import com.example.yawa.iam.user.service.UserService;

@Component
public class UserMessageController implements InitializingBean, DisposableBean {

  private final UserService userService;
  private final SqsClient sqsClient;
  private final AmazonSQSResponder sqsResponder;
  private final ObjectMapper sqsMessageMapper;
  private final String userGetByIdQueueUrl;

  private SQSMessageConsumer userGetByIdMessageConsumer;

  @Autowired
  public UserMessageController(
      UserService userService,
      SqsClient sqsClient,
      AmazonSQSResponder sqsResponder,
      ObjectMapper sqsMessageMapper,
      UserMessageControllerProperties properties
  ) {
    this.userService = userService;
    this.sqsClient = sqsClient;
    this.sqsResponder = sqsResponder;
    this.sqsMessageMapper = sqsMessageMapper;
    this.userGetByIdQueueUrl = properties.userGetByIdQueueUrl;
  }

  @Override
  public void afterPropertiesSet() {
    startGetByIdMessageConsumer();
  }

  @Override
  public void destroy() {
    stopGetByIdMessageConsumer();
  }

  public User getById(UserGetByIdMessage message) throws NotFoundException {
    UUID id = message.getId();

    return userService.getById(id);
  }

  public String getById(String message) {
    try {
      UserGetByIdMessage messageObj = readMessage(message, UserGetByIdMessage.class);
      User userObj = this.getById(messageObj);
      return writeMessage(userObj);
    } catch (NotFoundException e) {
      return "NOT_FOUND";
    }
  }

  private void startGetByIdMessageConsumer() {
    this.userGetByIdMessageConsumer = SQSMessageConsumerBuilder.standard()
        .withAmazonSQS(sqsClient)
        .withQueueUrl(userGetByIdQueueUrl)
        .withConsumer(message -> sqsResponder.sendResponseMessage(
            MessageContent.fromMessage(message),
            new MessageContent(getById(message.body()))
        ))
        .build();
    this.userGetByIdMessageConsumer.start();
  }

  private void stopGetByIdMessageConsumer() {
    this.userGetByIdMessageConsumer.close();
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


  public static class UserMessageControllerProperties {

    private final String userGetByIdQueueUrl;

    public UserMessageControllerProperties(String userGetByIdQueueUrl) {
      this.userGetByIdQueueUrl = userGetByIdQueueUrl;
    }

  }

}
