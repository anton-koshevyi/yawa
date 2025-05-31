package com.example.yawa.iam.session.exception;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundSessionByIdException extends NotFoundException {

  public NotFoundSessionByIdException(UUID id) {
    super("exception.notFound.session.byId", id);
  }

}
