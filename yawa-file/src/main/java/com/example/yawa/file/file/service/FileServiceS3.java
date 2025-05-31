package com.example.yawa.file.file.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.exception.NotFoundFileByIdException;
import com.example.yawa.file.file.model.File;

@Service
public class FileServiceS3 implements FileService {

  private static final String METADATA_KEY_USER_ID = "user-id";

  private static final Logger logger = LoggerFactory.getLogger(FileServiceS3.class);

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final Clock clock;
  private final String bucket;
  private final Integer urlExpirationSeconds;

  @Autowired
  public FileServiceS3(
      S3Client s3Client,
      S3Presigner s3Presigner,
      Clock clock,
      FileServiceS3Properties properties
  ) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.clock = clock;
    this.bucket = properties.bucket;
    this.urlExpirationSeconds = properties.urlExpirationSeconds;
  }

  @Override
  public File save(UUID userId, String filename, String contentType, byte[] content) {
    logger.debug("Save of file for user with ID: {}", userId);

    String key = userId + "/" + UUID.randomUUID();
    String contentDisposition = ContentDisposition.builder("attachment")
        .filename(filename, StandardCharsets.UTF_8)
        .build()
        .toString();
    Map<String, String> metadata = new LinkedHashMap<>();
    metadata.put(METADATA_KEY_USER_ID, userId.toString());

    s3Client.putObject(
        por -> por
            .bucket(bucket)
            .key(key)
            .contentDisposition(contentDisposition)
            .contentType(contentType)
            .metadata(metadata),
        RequestBody.fromBytes(content)
    );
    File file = doFindById(key);

    logger.info("File has been saved: {}", file);
    return file;
  }

  @Override
  public File findById(String id) throws NotFoundException {
    try {
      return doFindById(id);
    } catch (NoSuchKeyException e) {
      throw new NotFoundFileByIdException(id);
    }
  }

  @Override
  public List<File> findAllByUserId(UUID userId) {
    return s3Client
        .listObjects(lor -> lor
            .bucket(bucket)
            .prefix(userId.toString()))
        .contents()
        .stream()
        .map(S3Object::key)
        .map(this::doFindById)
        .collect(Collectors.toList());
  }

  private File doFindById(String id) {
    HeadObjectResponse object = s3Client
        .headObject(hor -> hor
            .bucket(bucket)
            .key(id));
    PresignedGetObjectRequest presignedObject = s3Presigner
        .presignGetObject(gopr -> gopr
            .getObjectRequest(gor -> gor
                .bucket(bucket)
                .key(id))
            .signatureDuration(Duration.ofSeconds(urlExpirationSeconds))
            .build());
    ZoneId zone = clock.getZone();

    return Assembler.assemble(id, object, presignedObject, zone);
  }


  public static class FileServiceS3Properties {

    private final String bucket;
    private final Integer urlExpirationSeconds;

    public FileServiceS3Properties(
        String bucket,
        Integer urlExpirationSeconds
    ) {
      this.bucket = bucket;
      this.urlExpirationSeconds = urlExpirationSeconds;
    }

  }

  private static class Assembler {

    static File assemble(
        String key,
        HeadObjectResponse object,
        PresignedGetObjectRequest presignedObject,
        ZoneId zone
    ) {
      File file = new File();
      file.setId(key);
      file.setUserId(getUserId(object));
      file.setName(getName(object));
      file.setContentType(object.contentType());
      file.setUrl(presignedObject.url());
      file.setUrlExpiredAt(OffsetDateTime.ofInstant(presignedObject.expiration(), zone));
      file.setCreatedAt(OffsetDateTime.ofInstant(object.lastModified(), zone));
      return file;
    }

    private static UUID getUserId(HeadObjectResponse object) {
      String userId = object.metadata().get(METADATA_KEY_USER_ID);
      return UUID.fromString(userId);
    }

    private static String getName(HeadObjectResponse object) {
      String contentDisposition = object.contentDisposition();
      return ContentDisposition.parse(contentDisposition).getFilename();
    }

  }

}
