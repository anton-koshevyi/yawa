package com.example.yawa.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("application")
public class ApplicationProperties {

  private TokenProperties token;


  @Getter
  @Setter
  public static class TokenProperties {

    private String serverSecret;
    private Integer serverInteger;

  }

}
