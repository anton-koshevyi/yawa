package com.example.yawa.iam.session.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.iam.session.repository.KeyRepositorySecretsManager.KeyRepositoryKmsProperties;

@Configuration("sessionApplicationConfig")
public class ApplicationConfig {

  @Bean
  public KeyRepositoryKmsProperties keyRepositoryKmsProperties(
      ApplicationProperties properties
  ) {
    return new KeyRepositoryKmsProperties(
        properties.getAws().getSecretsManager().getSecret().getSessionAccess().getName()
    );
  }

}
