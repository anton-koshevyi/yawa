package com.example.yawa.iam.session.repository;

import java.security.Key;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Profile("!development")
@Repository
public class KeyRepositorySecretsManager implements KeyRepository {

  private final SecretsManagerClient secretsManagerClient;
  private final String secretNameSessionAccess;
  private final String secretNameSessionRefresh;

  @Autowired
  public KeyRepositorySecretsManager(
      SecretsManagerClient secretsManagerClient,
      KeyRepositoryKmsProperties properties
  ) {
    this.secretsManagerClient = secretsManagerClient;
    this.secretNameSessionAccess = properties.secretNameSessionAccess;
    this.secretNameSessionRefresh = properties.secretNameSessionRefresh;
  }

  @Override
  public Optional<Key> findSessionAccess() {
    return Optional.of(getPublicKeyAes(secretNameSessionAccess));
  }

  @Override
  public Optional<Key> findSessionRefresh() {
    return Optional.of(getPublicKeyAes(secretNameSessionRefresh));
  }

  private Key getPublicKeyAes(String secretName) {
    GetSecretValueResponse secretValue = secretsManagerClient
        .getSecretValue(gsvr -> gsvr
            .secretId(secretName));
    byte[] keyBytes = secretValue
        .secretString()
        .getBytes();

    return new SecretKeySpec(keyBytes, "AES");
  }


  public static class KeyRepositoryKmsProperties {

    private final String secretNameSessionAccess;
    private final String secretNameSessionRefresh;

    public KeyRepositoryKmsProperties(
        String secretNameSessionAccess,
        String secretNameSessionRefresh
    ) {
      this.secretNameSessionAccess = secretNameSessionAccess;
      this.secretNameSessionRefresh = secretNameSessionRefresh;
    }

  }

}
