package com.example.yawa.file.file.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import com.example.yawa.file.file.constraint.MultipartFileExtension;
import com.example.yawa.file.file.constraint.MultipartFileNotNullFilename;
import com.example.yawa.file.file.constraint.MultipartFileSize;

@Getter
@RequiredArgsConstructor
public class FileUploadRequest {

  @NotNull
  @MultipartFileNotNullFilename
  @MultipartFileExtension({"pdf", "jpg", "jpeg"})
  @MultipartFileSize(10)
  private final MultipartFile file;

}
