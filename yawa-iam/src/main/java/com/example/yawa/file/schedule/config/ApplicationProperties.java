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
  private ScheduleProperties schedule;


  @Getter
  @Setter
  public static class AwsProperties {

    private SnsProperties sns;
    private SfnProperties sfn;


    @Getter
    @Setter
    public static class SnsProperties {

      private TopicProperties topic;


      @Getter
      @Setter
      public static class TopicProperties {

        private TopicSpecificProperties fileUpload;
        private TopicSpecificProperties fileDelete;


        @Getter
        @Setter
        public static class TopicSpecificProperties {

          private String arn;

        }

      }

    }

    @Getter
    @Setter
    public static class SfnProperties {

      private StateMachineProperties stateMachine;


      @Getter
      @Setter
      public static class StateMachineProperties {

        private StateMachineSpecificProperties fileDeleteScheduler;


        @Getter
        @Setter
        public static class StateMachineSpecificProperties {

          private String name;
          private StateMachineRoleProperties role;


          @Getter
          @Setter
          public static class StateMachineRoleProperties {

            private String arn;

          }

        }

      }

    }

  }

  @Getter
  @Setter
  public static class ScheduleProperties {

    private Integer fileDeletionSeconds;

  }

}
