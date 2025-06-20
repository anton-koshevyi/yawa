package com.example.yawa.iam.session.repository;

import java.security.Key;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("development")
@Repository
public class KeyRepositoryMemory implements KeyRepository {

  private static final String AES_ACCESS = "euxbHYC9OxMB+P0NJ83udHJnippQ2nTn";
  private static final String AES_REFRESH = "zF9opabhdfMyAaaQCK+0LLvjdqplW9Sn";

  private final Key aesAccess = new SecretKeySpec(AES_ACCESS.getBytes(), "AES");
  private final Key aesRefresh = new SecretKeySpec(AES_REFRESH.getBytes(), "AES");

  @Override
  public Optional<Key> findSessionAccess() {
    return Optional.of(aesAccess);
  }

  @Override
  public Optional<Key> findSessionRefresh() {
    return Optional.of(aesRefresh);
  }

}
