package com.example.yawa.file.file.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.example.yawa.file.file.model.File;

@Getter
@RequiredArgsConstructor
public class FileDeleteEvent {

  private final File file;

}
