package com.example.yawa.file.file.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.exception.NotFoundFileByIdAndUserIdException;

@Service
public class FileServiceRemote implements FileService {

  @Override
  public void assertExistsByIdAndUserId(String id, UUID userId) throws NotFoundException {
    // Simplified implementation instead of calling the file microservice
    if (!id.startsWith(userId.toString())) {
      throw new NotFoundFileByIdAndUserIdException(id, userId);
    }
  }

}
