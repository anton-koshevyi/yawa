package com.example.yawa.iam.user.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("userApplicationProperties")
@ConfigurationProperties("application")
public class ApplicationProperties {

  private AwsProperties aws;
  private TokenProperties token;
  private ScheduleProperties schedule;


  @Getter
  @Setter
  public static class AwsProperties {

    private LambdaProperties lambda;
    private SnsProperties sns;
    private SqsProperties sqs;


    @Getter
    @Setter
    public static class LambdaProperties {

      private LambdaSpecificProperties email;

      @Getter
      @Setter
      public static class LambdaSpecificProperties {

        private String arn;

      }

    }

    @Getter
    @Setter
    public static class SnsProperties {

      private TopicProperties topic;


      @Getter
      @Setter
      public static class TopicProperties {

        private TopicSpecificProperties email;


        @Getter
        @Setter
        public static class TopicSpecificProperties {

          private String arn;

        }

      }

    }

    @Getter
    @Setter
    public static class SqsProperties {

      private QueueProperties queue;


      @Getter
      @Setter
      public static class QueueProperties {

        private QueueSpecificProperties userGetById;


        @Getter
        @Setter
        public static class QueueSpecificProperties {

          private String url;

        }

      }

    }


  }

  @Getter
  @Setter
  public static class TokenProperties {

    private Integer emailRequestVerificationExpirationSeconds;

  }

  @Getter
  @Setter
  public static class ScheduleProperties {

    private Duration emailRequestDeletionRate;

  }

}
