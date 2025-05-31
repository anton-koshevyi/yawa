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

  private AwsProperties aws;
  private FileProperties file;


  @Getter
  @Setter
  public static class AwsProperties {

    private S3Properties s3;


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

  }

  @Getter
  @Setter
  public static class FileProperties {

    private Integer urlExpirationSeconds;

  }

}
