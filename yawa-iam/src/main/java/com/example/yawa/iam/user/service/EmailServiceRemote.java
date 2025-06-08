package com.example.yawa.iam.user.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;

@Service
public class EmailServiceRemote implements EmailService, InitializingBean, DisposableBean {

  private static final Logger logger = LoggerFactory.getLogger(EmailServiceRemote.class);

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
  public void sendSignUpInit(String email, String verificationToken) {
    logger.debug("Publish of message to send sign-up init email: {}", email);

    Map<String, String> input = new LinkedHashMap<>();
    input.put("recipient", email);
    input.put("subject", "Email confirmation");
    input.put("body", "Verification token: " + verificationToken);
    publish(input);

    logger.info("Message to send sign-up init email has been published: {}", email);
  }

  @Override
  public void sendSignUpComplete(String email) {
    logger.debug("Publish of message to send sign-up complete email: {}", email);

    Map<String, String> input = new LinkedHashMap<>();
    input.put("recipient", email);
    input.put("subject", "Registration complete");
    input.put("body", "Registration completed");
    publish(input);

    logger.info("Message to send sign-up complete email has been published: {}", email);
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
