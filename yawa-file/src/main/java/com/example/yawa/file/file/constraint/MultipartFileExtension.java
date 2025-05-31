package com.example.yawa.file.file.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.example.yawa.file.file.validator.MultipartFileExtensionValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Constraint(validatedBy = MultipartFileExtensionValidator.class)
public @interface MultipartFileExtension {

  String[] value();

  String message() default "multipart file extension must be in {value}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
