package com.example.yawa.file.file.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;
import com.example.yawa.file.file.dto.event.FileUploadEvent;
import com.example.yawa.file.file.dto.request.FileUploadRequest;
import com.example.yawa.file.file.model.File;
import com.example.yawa.file.file.service.FileService;

@RestController
public class FileController {

  private final FileService fileService;
  private final SpringValidatorAdapter springValidatorAdapter;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public FileController(
      FileService fileService,
      SpringValidatorAdapter springValidatorAdapter,
      ApplicationEventPublisher applicationEventPublisher
  ) {
    this.fileService = fileService;
    this.springValidatorAdapter = springValidatorAdapter;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @GetMapping("/files")
  @PreAuthorize("isAuthenticated()")
  public List<File> findAll(SecurityExpressionRootExtended security) {
    UUID userId = security.getUserId();

    return fileService.findAllByUserId(userId);
  }

  @PostMapping("/files")
  @PreAuthorize("isAuthenticated()")
  public void upload(
      @RequestPart MultipartFile multipartFile,
      SecurityExpressionRootExtended security
  ) throws BindException, IOException {
    FileUploadRequest uploadRequest = new FileUploadRequest(multipartFile);
    BindingResult bindingResult = new BeanPropertyBindingResult(null, "");
    springValidatorAdapter.validate(uploadRequest, bindingResult);

    if (bindingResult.hasErrors()) {
      throw new BindException(bindingResult);
    }

    UUID userId = security.getUserId();
    String filename = multipartFile.getOriginalFilename();
    String contentType = multipartFile.getContentType();
    byte[] content = multipartFile.getBytes();

    File file = fileService.save(userId, filename, contentType, content);

    Object event = new FileUploadEvent(file);
    applicationEventPublisher.publishEvent(event);
  }

}
