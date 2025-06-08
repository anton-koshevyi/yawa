package com.example.yawa.file.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("fileApplicationProperties")
@ConfigurationProperties("application")
public class ApplicationProperties {

  private String apiBaseUrl;
  private AwsProperties aws;
  private FileProperties file;


  @Getter
  @Setter
  public static class AwsProperties {

    private LambdaProperties lambda;
    private S3Properties s3;
    private SnsProperties sns;


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
    public static class S3Properties {

      private BucketProperties bucket;


      @Getter
      @Setter
      public static class BucketProperties {

        private String file;

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
        private TopicSpecificProperties fileUpload;
        private TopicSpecificProperties fileDelete;


        @Getter
        @Setter
        public static class TopicSpecificProperties {

          private String arn;

        }

      }

    }

  }

  @Getter
  @Setter
  public static class FileProperties {

    private Integer urlExpirationSeconds;

  }

}
