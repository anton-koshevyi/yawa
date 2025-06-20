package com.example.yawa.file.schedule.service;

import com.example.yawa.application.exception.NotFoundException;

public interface FileScheduleService {

  void createDeletion(String fileId);

  void postponeDeletion(String fileId) throws NotFoundException;

}
