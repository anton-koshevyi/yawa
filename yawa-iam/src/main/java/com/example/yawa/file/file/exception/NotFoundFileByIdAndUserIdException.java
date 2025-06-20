package com.example.yawa.file.file.exception;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundFileByIdAndUserIdException extends NotFoundException {

  public NotFoundFileByIdAndUserIdException(String id, UUID userId) {
    super("exception.notFound.file.byIdAndUserId", id, userId);
  }

}
