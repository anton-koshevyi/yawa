package com.example.yawa.iam.user.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.iam.user.config.ApplicationProperties;
import com.example.yawa.iam.user.exception.NotFoundEmailRequestByIdException;
import com.example.yawa.iam.user.model.EmailRequest;
import com.example.yawa.iam.user.repository.EmailRequestRepository;

@Service
public class EmailRequestService {

  private static final Logger logger = LoggerFactory.getLogger(EmailRequestService.class);

  private final EmailRequestRepository emailRequestRepository;
  private final Clock clock;
  private final Integer verificationTokenExpirationSeconds;

  @Autowired
  public EmailRequestService(
      EmailRequestRepository emailRequestRepository,
      Clock clock,
      ApplicationProperties properties
  ) {
    this.emailRequestRepository = emailRequestRepository;
    this.clock = clock;
    this.verificationTokenExpirationSeconds = properties.getToken()
        .getEmailRequestVerificationExpirationSeconds();
  }

  public EmailRequest create(String actualEmail, String requestedEmail) {
    logger.debug("Create of email-request for email: {}", requestedEmail);

    EmailRequest request = new EmailRequest();
    request.setActualEmail(actualEmail);
    request.setRequestedEmail(requestedEmail);
    request.setCreatedAt(OffsetDateTime.now(clock));
    UUID createdRequestId = emailRequestRepository.create(request);

    logger.info("Email-request has been created for email: {}", requestedEmail);
    return emailRequestRepository.findById(createdRequestId)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find email request by created ID: " + createdRequestId));
  }

  public void deleteAllByRequestedEmail(String requestedEmail) {
    logger.debug("Delete of all email-requests for requested email: {}", requestedEmail);

    emailRequestRepository.deleteAllByRequestedEmail(requestedEmail);

    logger.info("All email-requests have been deleted for requested email: {}", requestedEmail);
  }

  public void deleteAllExpired() {
    logger.debug("Delete of all expired email requests");

    OffsetDateTime expiredAt = OffsetDateTime.now(clock)
        .minusSeconds(verificationTokenExpirationSeconds);

    emailRequestRepository.deleteAllByCreatedAtBefore(expiredAt);

    logger.info("All expired email requests have been deleted");
  }

  public EmailRequest getById(UUID id) throws NotFoundException {
    return emailRequestRepository.findById(id)
        .orElseThrow(() -> new NotFoundEmailRequestByIdException(id));
  }

}
