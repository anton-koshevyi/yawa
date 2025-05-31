package com.example.yawa.file.file.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.example.yawa.file.file.model.File;

@Getter
@RequiredArgsConstructor
public class FileUploadEvent {

  private final File file;

}
