package com.example.yawa.file.schedule.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("fileScheduleApplicationProperties")
@ConfigurationProperties("application")
public class ApplicationProperties {

  private String apiBaseUrl;
  private AwsProperties aws;


  @Getter
  @Setter
  public static class AwsProperties {

    private SnsProperties sns;


    @Getter
    @Setter
    public static class SnsProperties {

      private TopicProperties topic;


      @Getter
      @Setter
      public static class TopicProperties {

        private TopicSpecificProperties fileUpload;


        @Getter
        @Setter
        public static class TopicSpecificProperties {

          private String arn;

        }

      }

    }

  }

}
