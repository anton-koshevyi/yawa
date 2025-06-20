package com.example.yawa.iam.session.repository;

import java.security.Key;
import java.util.Optional;

public interface KeyRepository {

  Optional<Key> findSessionAccess();

  Optional<Key> findSessionRefresh();

}
