package com.example.yawa.application.config;

import java.time.Clock;
import java.util.TimeZone;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
