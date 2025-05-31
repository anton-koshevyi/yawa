package com.example.yawa.file.file.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.example.yawa.file.file.validator.MultipartFileNotNullFilenameValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = MultipartFileNotNullFilenameValidator.class)
public @interface MultipartFileNotNullFilename {

  String message() default "multipart file filename must not be null";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
