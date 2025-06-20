package com.example.yawa.file.file.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.yawa.file.file.constraint.MultipartFileSize;

public class MultipartFileSizeValidator
    implements ConstraintValidator<MultipartFileSize, MultipartFile> {

  private int maxSizeMegabytes;

  @Override
  public void initialize(MultipartFileSize annotation) {
    this.maxSizeMegabytes = annotation.value();
  }

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    long sizeBytes = value.getSize();
    long sizeMegabytes = sizeBytes / FileUtils.ONE_MB;

    return sizeMegabytes <= maxSizeMegabytes;
  }

}
