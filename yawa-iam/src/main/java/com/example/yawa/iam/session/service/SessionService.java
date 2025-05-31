package com.example.yawa.iam.session.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.session.exception.NotFoundSessionByIdException;
import com.example.yawa.iam.session.model.Session;
import com.example.yawa.iam.session.repository.SessionRepository;

@Service
public class SessionService {

  private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

  private final SessionRepository sessionRepository;
  private final Clock clock;

  @Autowired
  public SessionService(SessionRepository sessionRepository, Clock clock) {
    this.sessionRepository = sessionRepository;
    this.clock = clock;
  }

  public Session create(UUID userId) {
    logger.debug("Create of session for user with ID: {}", userId);

    Session session = new Session();
    session.setAccessedAt(OffsetDateTime.now(clock));
    session.setUserId(userId);
    UUID createdSessionId = sessionRepository.create(session);

    logger.info("Session has been created for user with ID: {}", userId);
    return sessionRepository.findById(createdSessionId)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find session by created ID: " + createdSessionId));
  }

  public Session update(UUID id) throws NotFoundException {
    logger.debug("Update of session for ID: {}", id);

    Session session = getById(id);
    session.setAccessedAt(OffsetDateTime.now(clock));
    sessionRepository.update(session);

    logger.info("Session has been updated for ID: {}", id);
    return session;
  }

  public void deleteAllByIds(UUID[] ids) {
    logger.debug("Delete of all sessions for IDs: {}", (Object[]) ids);

    sessionRepository.deleteAllByIds(ids);

    logger.info("All sessions have been deleted for IDs: {}", (Object[]) ids);
  }

  private Session getById(UUID id) throws NotFoundException {
    return sessionRepository.findById(id)
        .orElseThrow(() -> new NotFoundSessionByIdException(id));
  }

}
