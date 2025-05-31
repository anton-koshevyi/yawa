package com.example.yawa.file.file.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.model.File;
import com.example.yawa.file.file.repository.FileMetadataRepository;

@Primary
@Service
public class FileServiceMetadata implements FileService {

  private static final Logger logger = LoggerFactory.getLogger(FileServiceMetadata.class);

  private final FileService fileService;
  private final FileMetadataRepository fileMetadataRepository;
  private final Clock clock;

  @Autowired
  public FileServiceMetadata(
      FileService fileService,
      FileMetadataRepository fileMetadataRepository,
      Clock clock
  ) {
    this.fileService = fileService;
    this.fileMetadataRepository = fileMetadataRepository;
    this.clock = clock;
  }

  @Override
  public File save(UUID userId, String filename, String contentType, byte[] content) {
    File file = fileService.save(userId, filename, contentType, content);
    logger.debug("Create of metadata for file: {}", file);

    fileMetadataRepository.create(file);

    logger.info("Metadata has been created for file: {}", file);
    return file;
  }

  @Override
  public File findById(String id) throws NotFoundException {
    Optional<File> metadataOptional = fileMetadataRepository.findById(id);

    if (!metadataOptional.isPresent()) {
      File file = fileService.findById(id);
      logger.warn("Found absent metadata for file with ID: {}", id);

      fileMetadataRepository.create(file);
      return file;
    }

    File metadata = metadataOptional.get();
    OffsetDateTime urlExpiredAt = metadata.getUrlExpiredAt();
    OffsetDateTime requestedAt = OffsetDateTime.now(clock);

    if (requestedAt.isAfter(urlExpiredAt)) {
      try {
        File file = fileService.findById(id);
        logger.debug("Found expired metadata for file with ID: {}", id);

        fileMetadataRepository.update(file);

        logger.info("Metadata has been refreshed for file with ID: {}", id);
        return file;
      } catch (NotFoundException e) {
        logger.warn("Found metadata for non-existent file with ID: {}", id);
        fileMetadataRepository.delete(metadata);
        throw e;
      }
    }

    return metadata;
  }

  @Override
  public List<File> findAllByUserId(UUID userId) {
    List<File> metadata = fileMetadataRepository.findAllByUserId(userId);

    if (metadata.isEmpty()) {
      List<File> files = fileService.findAllByUserId(userId);

      if (!files.isEmpty()) {
        logger.warn("Found absent metadata for user with ID: {}", userId);
        fileMetadataRepository.createAll(files);
      }

      return files;
    }

    OffsetDateTime requestedAt = OffsetDateTime.now(clock);
    boolean containsExpired = metadata.stream()
        .map(File::getUrlExpiredAt)
        .anyMatch(requestedAt::isAfter);

    if (containsExpired) {
      logger.debug("Found expired metadata for user with ID: {}", userId);

      List<File> files = fileService.findAllByUserId(userId);
      fileMetadataRepository.replaceAll(metadata, files);

      logger.info("Metadata has been refreshed for user with ID: {}", userId);
      return files;
    }

    return metadata;
  }

}
