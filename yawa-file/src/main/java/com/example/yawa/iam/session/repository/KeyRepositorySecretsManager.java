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

  @Autowired
  public KeyRepositorySecretsManager(
      SecretsManagerClient secretsManagerClient,
      KeyRepositoryKmsProperties properties
  ) {
    this.secretsManagerClient = secretsManagerClient;
    this.secretNameSessionAccess = properties.secretNameSessionAccess;
  }

  @Override
  public Optional<Key> findSessionAccess() {
    return Optional.of(getPublicKeyAes(secretNameSessionAccess));
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

    public KeyRepositoryKmsProperties(String secretNameSessionAccess) {
      this.secretNameSessionAccess = secretNameSessionAccess;
    }

  }

}
