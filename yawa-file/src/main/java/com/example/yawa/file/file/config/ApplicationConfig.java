package com.example.yawa.file.file.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.yawa.file.file.config.ApplicationProperties.AwsProperties;
import com.example.yawa.file.file.config.ApplicationProperties.AwsProperties.SnsProperties.TopicProperties;
import com.example.yawa.file.file.config.ApplicationProperties.AwsProperties.SqsProperties.QueueProperties;
import com.example.yawa.file.file.config.ApplicationProperties.AwsProperties.SqsProperties.QueueProperties.QueueSpecificProperties;
import com.example.yawa.file.file.controller.FileEventController.FileEventControllerProperties;
import com.example.yawa.file.file.repository.FileMetadataRepository.FileMetadataRepositoryProperties;
import com.example.yawa.file.file.service.EmailServiceRemote.EmailServiceRemoteProperties;
import com.example.yawa.file.file.service.FileServiceS3.FileServiceS3Properties;
import com.example.yawa.iam.user.service.UserServiceRemote.UserServiceRemoteProperties;

@Configuration("fileApplicationConfig")
public class ApplicationConfig {

  @Bean
  public FileEventControllerProperties fileEventControllerProperties(
      ApplicationProperties properties
  ) {
    TopicProperties topicProperties = properties.getAws().getSns().getTopic();

    return new FileEventControllerProperties(
        topicProperties.getFileUpload().getArn(),
        topicProperties.getFileDelete().getArn(),
        properties.getApiBaseUrl()
    );
  }

  @Bean
  public FileServiceS3Properties fileServiceS3Properties(ApplicationProperties properties) {
    return new FileServiceS3Properties(
        properties.getAws().getS3().getBucket().getFile(),
        properties.getFile().getUrlExpirationSeconds()
    );
  }

  @Bean
  public EmailServiceRemoteProperties emailServiceRemoteProperties(
      ApplicationProperties applicationProperties
  ) {
    AwsProperties awsProperties = applicationProperties.getAws();

    return new EmailServiceRemoteProperties(
        awsProperties.getSns().getTopic().getEmail().getArn(),
        awsProperties.getLambda().getEmail().getArn()
    );
  }

  @Bean
  public UserServiceRemoteProperties userServiceRemoteProperties(
      ApplicationProperties properties
  ) {
    QueueProperties queueProperties = properties.getAws().getSqs().getQueue();
    QueueSpecificProperties userGetByIdQueueProperties = queueProperties.getUserGetById();

    return new UserServiceRemoteProperties(
        userGetByIdQueueProperties.getUrl(),
        userGetByIdQueueProperties.getGroupId()
    );
  }

  @Bean
  public FileMetadataRepositoryProperties fileMetadataRepositoryProperties(
      MongoProperties properties
  ) {
    return new FileMetadataRepositoryProperties(properties.getDatabase());
  }

}
