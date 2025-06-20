package com.example.yawa.iam.session.config;

import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("sessionApplicationProperties")
@ConfigurationProperties("application")
public class ApplicationProperties {

  private AwsProperties aws;
  private TokenProperties token;
  private ScheduleProperties schedule;


  @Getter
  @Setter
  public static class AwsProperties {

    private SecretsManagerProperties secretsManager;


    @Getter
    @Setter
    public static class SecretsManagerProperties {

      private SecretProperties secret;


      @Getter
      @Setter
      public static class SecretProperties {

        private SecretSpecificProperties sessionAccess;
        private SecretSpecificProperties sessionRefresh;


        @Getter
        @Setter
        public static class SecretSpecificProperties {

          private String name;

        }

      }

    }

  }

  @Getter
  @Setter
  public static class TokenProperties {

    private Integer sessionAccessExpirationSeconds;
    private Integer sessionRefreshExpirationSeconds;

  }

  @Getter
  @Setter
  public static class ScheduleProperties {

    private Duration sessionRefreshTokenDeletionRate;

  }

}
