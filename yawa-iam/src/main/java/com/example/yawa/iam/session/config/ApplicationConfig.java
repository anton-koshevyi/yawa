package com.example.yawa.iam.session.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.iam.session.config.ApplicationProperties.AwsProperties.SecretsManagerProperties.SecretProperties;
import com.example.yawa.iam.session.repository.KeyRepositorySecretsManager.KeyRepositoryKmsProperties;

@Configuration("sessionApplicationConfig")
public class ApplicationConfig {

  @Bean
  public KeyRepositoryKmsProperties keyRepositoryKmsProperties(
      ApplicationProperties properties
  ) {
    SecretProperties secretProperties = properties.getAws().getSecretsManager().getSecret();

    return new KeyRepositoryKmsProperties(
        secretProperties.getSessionAccess().getName(),
        secretProperties.getSessionRefresh().getName()
    );
  }

}
