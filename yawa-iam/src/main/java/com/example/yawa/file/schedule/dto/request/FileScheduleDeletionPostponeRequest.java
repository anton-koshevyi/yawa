package com.example.yawa.file.schedule.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileScheduleDeletionPostponeRequest {

  @NotNull
  private String fileId;

}
