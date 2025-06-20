package com.example.yawa.file.file.exception;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundFileByIdException extends NotFoundException {

  public NotFoundFileByIdException(String id) {
    super("exception.notFound.file.byId", id);
  }

}
