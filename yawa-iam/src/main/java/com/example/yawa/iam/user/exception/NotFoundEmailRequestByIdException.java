package com.example.yawa.iam.user.exception;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundEmailRequestByIdException extends NotFoundException {

  public NotFoundEmailRequestByIdException(UUID id) {
    super("exception.notFound.emailRequest.byId", id);
  }

}
