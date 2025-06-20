package com.example.yawa.file.schedule.controller;

import java.util.UUID;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;
import com.example.yawa.application.exception.NotFoundException;
import com.example.yawa.file.file.service.FileService;
import com.example.yawa.file.schedule.dto.request.FileScheduleDeletionPostponeRequest;
import com.example.yawa.file.schedule.service.FileScheduleService;

@RestController
public class FileScheduleController {

  private final FileScheduleService fileScheduleService;
  private final FileService fileService;

  @Autowired
  public FileScheduleController(
      FileScheduleService fileScheduleService,
      FileService fileService
  ) {
    this.fileScheduleService = fileScheduleService;
    this.fileService = fileService;
  }

  @PutMapping("/file-schedules/deletions")
  @PreAuthorize("isAuthenticated()")
  public void postponeDeletion(
      @Valid @RequestBody FileScheduleDeletionPostponeRequest request,
      SecurityExpressionRootExtended security
  ) throws NotFoundException {
    UUID userId = security.getUserId();
    String fileId = request.getFileId();

    fileService.assertExistsByIdAndUserId(fileId, userId);
    fileScheduleService.postponeDeletion(fileId);
  }

}
