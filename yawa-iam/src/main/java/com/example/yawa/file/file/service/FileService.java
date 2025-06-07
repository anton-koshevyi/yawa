package com.example.yawa.file.file.service;

import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;

public interface FileService {

  void assertExistsByIdAndUserId(String id, UUID userId) throws NotFoundException;

}
