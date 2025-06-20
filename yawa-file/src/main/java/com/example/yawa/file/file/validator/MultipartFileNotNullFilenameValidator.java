package com.example.yawa.file.file.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

import com.example.yawa.file.file.constraint.MultipartFileNotNullFilename;

public class MultipartFileNotNullFilenameValidator
    implements ConstraintValidator<MultipartFileNotNullFilename, MultipartFile> {

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    String filename = value.getOriginalFilename();

    return filename != null;
  }

}
