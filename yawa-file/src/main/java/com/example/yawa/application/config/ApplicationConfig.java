package com.example.yawa.application.config;

import java.time.Clock;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sns.SnsClient;

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
  public S3Client s3Client() {
    return S3Client.create();
  }

  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.create();
  }

  @Bean
  public SnsClient snsClient() {
    return SnsClient.create();
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

}
