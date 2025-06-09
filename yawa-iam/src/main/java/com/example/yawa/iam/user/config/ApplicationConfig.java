package com.example.yawa.iam.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.iam.user.config.ApplicationProperties.AwsProperties;
import com.example.yawa.iam.user.controller.UserMessageController.UserMessageControllerProperties;
import com.example.yawa.iam.user.service.EmailServiceRemote.EmailServiceRemoteProperties;

@Configuration("userApplicationConfig")
public class ApplicationConfig {

  @Bean
  public UserMessageControllerProperties userMessageControllerProperties(
      ApplicationProperties properties
  ) {
    return new UserMessageControllerProperties(
        properties.getAws().getSqs().getQueue().getUserGetById().getUrl()
    );
  }

  @Bean
  public EmailServiceRemoteProperties emailServiceRemoteProperties(
      ApplicationProperties properties
  ) {
    AwsProperties aws = properties.getAws();

    return new EmailServiceRemoteProperties(
        aws.getSns().getTopic().getEmail().getArn(),
        aws.getLambda().getEmail().getArn()
    );
  }

}
