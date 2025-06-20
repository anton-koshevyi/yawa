package com.example.yawa.iam.user.exception;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundUserByIdException extends NotFoundException {

  public NotFoundUserByIdException(UUID id) {
    super("exception.notFound.user.byId", id);
  }

}
