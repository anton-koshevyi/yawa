package com.example.yawa.file.file.service;

import java.util.List;
import java.util.UUID;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.model.File;

public interface FileService {

  File save(UUID userId, String filename, String contentType, byte[] content);

  File delete(String id) throws NotFoundException;

  File findById(String id) throws NotFoundException;

  List<File> findAllByUserId(UUID userId);

}
