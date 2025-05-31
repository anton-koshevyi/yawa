package com.example.yawa.file.file.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.example.yawa.file.file.validator.MultipartFileSizeValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = MultipartFileSizeValidator.class)
public @interface MultipartFileSize {

  int value(); // Megabytes

  String message() default "multipart file size must not exceed {value} megabytes";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
