package com.example.yawa.file.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.file.schedule.controller.FileScheduleEventController.FileScheduleEventControllerProperties;

@Configuration("fileScheduleApplicationConfig")
public class ApplicationConfig {

  @Bean
  public FileScheduleEventControllerProperties fileScheduleEventControllerProperties(
      ApplicationProperties properties
  ) {
    return new FileScheduleEventControllerProperties(
        properties.getAws().getSns().getTopic().getFileUpload().getArn(),
        properties.getApiBaseUrl()
    );
  }

}
