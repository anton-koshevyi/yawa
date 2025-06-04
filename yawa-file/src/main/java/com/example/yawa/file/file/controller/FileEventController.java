package com.example.yawa.file.file.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;

import com.example.yawa.file.file.dto.event.FileUploadEvent;
import com.example.yawa.file.file.model.File;

@Component
public class FileEventController {

  private final SnsClient snsClient;
  private final ObjectMapper snsMessageMapper;
  private final String fileUploadTopicArn;

  @Autowired
  public FileEventController(
      SnsClient snsClient,
      ObjectMapper snsMessageMapper,
      FileEventControllerProperties properties
  ) {
    this.snsClient = snsClient;
    this.snsMessageMapper = snsMessageMapper;
    this.fileUploadTopicArn = properties.fileUploadTopicArn;
  }

  @EventListener
  public void consumeUpload(FileUploadEvent event) {
    File file = event.getFile();

    snsClient
        .publish(pr -> pr
            .topicArn(fileUploadTopicArn)
            .message(writeMessage(file)));
  }

  private String writeMessage(Object object) {
    try {
      return snsMessageMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to write object as SNS message", e);
    }
  }


  public static class FileEventControllerProperties {

    private final String fileUploadTopicArn;

    public FileEventControllerProperties(String fileUploadTopicArn) {
      this.fileUploadTopicArn = fileUploadTopicArn;
    }

  }

}
