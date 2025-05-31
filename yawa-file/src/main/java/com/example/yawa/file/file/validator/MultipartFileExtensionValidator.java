package com.example.yawa.file.file.validator;

import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.yawa.file.file.constraint.MultipartFileExtension;

public class MultipartFileExtensionValidator
    implements ConstraintValidator<MultipartFileExtension, MultipartFile> {

  private String[] extensions;

  @Override
  public void initialize(MultipartFileExtension annotation) {
    this.extensions = annotation.value();
  }

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    String filename = value.getOriginalFilename();
    String fileExtension = FilenameUtils.getExtension(filename);

    return Arrays.stream(extensions)
        .anyMatch(e -> e.equalsIgnoreCase(fileExtension));
  }

}
