package com.example.yawa.file.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.file.schedule.config.ApplicationProperties.AwsProperties;
import com.example.yawa.file.schedule.config.ApplicationProperties.AwsProperties.SfnProperties.StateMachineProperties.StateMachineSpecificProperties;
import com.example.yawa.file.schedule.controller.FileScheduleEventController.FileScheduleEventControllerProperties;
import com.example.yawa.file.schedule.service.FileScheduleServiceSfn.FileScheduleServiceSfnProperties;

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

  @Bean
  public FileScheduleServiceSfnProperties fileScheduleServiceSfnProperties(
      ApplicationProperties properties
  ) {
    AwsProperties awsProperties = properties.getAws();
    StateMachineSpecificProperties fileDeleteSchedulerProperties =
        awsProperties.getSfn().getStateMachine().getFileDeleteScheduler();

    return new FileScheduleServiceSfnProperties(
        fileDeleteSchedulerProperties.getName(),
        fileDeleteSchedulerProperties.getRole().getArn(),
        properties.getSchedule().getFileDeletionSeconds(),
        awsProperties.getSns().getTopic().getFileDelete().getArn()
    );
  }

}
