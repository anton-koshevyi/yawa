package com.example.yawa.file.schedule.exception;

import com.example.yawa.application.exception.NotFoundException;

public class NotFoundFileScheduleByFileIdException extends NotFoundException {

  public NotFoundFileScheduleByFileIdException(String fileId) {
    super("exception.notFound.fileSchedule.byFileId", fileId);
  }

}
