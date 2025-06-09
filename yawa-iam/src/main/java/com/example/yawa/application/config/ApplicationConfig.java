package com.example.yawa.application.config;

import java.time.Clock;
import java.util.TimeZone;

import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.AmazonSQSResponderClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class ApplicationConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public InitializingBean setDefaultTimezone(Clock clock) {
    TimeZone timeZone = TimeZone.getTimeZone(clock.getZone());

    return () -> TimeZone.setDefault(timeZone);
  }

  @Bean
  public SnsClient snsClient() {
    return SnsClient.create();
  }

  @Bean // Signature verification only
  public SnsMessageManager snsMessageManager(SnsClient snsClient) {
    return new SnsMessageManager(snsClient.serviceClientConfiguration().region().id());
  }

  @Bean
  public ObjectMapper snsMessageMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CASE)
        .setDefaultPropertyInclusion(Include.NON_NULL)
        .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(SerializationFeature.INDENT_OUTPUT);
  }

  @Bean
  public SfnClient sfnClient() {
    return SfnClient.create();
  }

  @Bean
  public SqsClient sqsClient() {
    return SqsClient.create();
  }

  @Bean
  public AmazonSQSResponder sqsResponder() {
    return AmazonSQSResponderClientBuilder.defaultClient();
  }

  @Bean
  public ObjectMapper sqsMessageMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setDefaultPropertyInclusion(Include.NON_NULL)
        .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.INDENT_OUTPUT);
  }

}
