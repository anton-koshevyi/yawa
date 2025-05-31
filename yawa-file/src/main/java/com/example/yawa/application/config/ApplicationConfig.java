package com.example.yawa.application.config;

import java.time.Clock;
import java.util.TimeZone;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

}
