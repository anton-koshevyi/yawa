package com.example.springframework.springframework.web.servlet.handler;

import java.util.Collections;
import java.util.Locale;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.springframework.web.servlet.handler.LocalizedHandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
public class LocalizedHandlerExceptionResolverTest {

  private @Mock MessageSource messageSource;

  @BeforeEach
  public void setUp() {
    RestAssuredMockMvc.mockMvc(MockMvcBuilders
        .standaloneSetup(new TestController())
        .setHandlerExceptionResolvers(new LocalizedHandlerExceptionResolver(messageSource))
        .addDispatcherServletCustomizer(c -> c
            .setThrowExceptionIfNoHandlerFound(true))
        .setValidator(new TargetValidator())
        .alwaysDo(MockMvcResultHandlers.log())
        .build());
  }

  @Test
  public void handleHttpRequestMethodNotSupported() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.httpRequestMethodNotSupported",
            new Object[]{"POST"},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .post("/httpRequestMethodNotSupported")
        .then()
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .header("Allow", "GET")
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(HttpRequestMethodNotSupportedException.class)
                .hasFieldOrPropertyWithValue("getMethod", "POST")
                .hasFieldOrPropertyWithValue("getSupportedMethods", new String[]{"GET"}),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleHttpMediaTypeNotSupported() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.httpMediaTypeNotSupported",
            new Object[]{MediaType.valueOf("text/plain;charset=UTF-8")},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .header("Content-Type", "text/plain;charset=UTF-8")
        .body("{ \"notBlank\": \"\" }")
        .when()
        .post("/httpMediaTypeNotSupported")
        .then()
        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .header("Accept", "application/json")
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(HttpMediaTypeNotSupportedException.class)
                .hasFieldOrPropertyWithValue("contentType",
                    MediaType.valueOf("text/plain;charset=UTF-8"))
                .hasFieldOrPropertyWithValue("getSupportedMediaTypes",
                    Collections.singletonList(MediaType.APPLICATION_JSON)),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleHttpMediaTypeNotAcceptable() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.httpMediaTypeNotAcceptable",
            null,
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept", "text/plain")
        .header("Accept-Language", "en")
        .when()
        .get("/httpMediaTypeNotAcceptable")
        .then()
        .status(HttpStatus.NOT_ACCEPTABLE)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(HttpMediaTypeNotAcceptableException.class)
                .hasFieldOrPropertyWithValue("getSupportedMediaTypes",
                    Collections.singletonList(MediaType.APPLICATION_JSON)),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleMissingPathVariable() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.serverError",
            null,
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .get("/missingPathVariable")
        .then()
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MissingPathVariableException.class)
                .hasFieldOrPropertyWithValue("getVariableName", "var"),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleMissingServletRequestParameter() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.missingServletRequestParameter",
            new Object[]{"param"},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .get("/missingServletRequestParameter")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MissingServletRequestParameterException.class)
                .hasFieldOrPropertyWithValue("getParameterName", "param"),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleServletRequestBinding() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.servletRequestBinding",
            null,
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .get("/servletRequestBinding")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MissingRequestCookieException.class),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleHttpMessageNotReadable() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.httpMessageNotReadable",
            null,
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .post("/httpMessageNotReadable")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(HttpMessageNotReadableException.class),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleTypeMismatch() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.typeMismatch",
            null,
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .queryParam("intParam", "string")
        .when()
        .get("/typeMismatch")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MethodArgumentTypeMismatchException.class),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleMethodArgumentNotValid() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.bind",
            new Object[]{1},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .header("Content-Type", "application/json")
        .body("{ \"notBlank\": \"\" }")
        .when()
        .post("/methodArgumentNotValid")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MethodArgumentNotValidException.class)
                .hasFieldOrPropertyWithValue("bindingResult.getErrorCount", 1),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleMissingServletRequestPart() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.missingServletRequestPart",
            new Object[]{"part"},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .header("Content-Type", "multipart/*")
        .when()
        .post("/missingServletRequestPart")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(MissingServletRequestPartException.class)
                .hasFieldOrPropertyWithValue("getRequestPartName", "part"),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleBind() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.bind",
            new Object[]{1},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .queryParam("notBlank", "")
        .when()
        .post("/bind")
        .then()
        .status(HttpStatus.BAD_REQUEST)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(BindException.class)
                .hasFieldOrPropertyWithValue("getErrorCount", 1),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }

  @Test
  public void handleNoHandlerFound() {
    Mockito
        .when(messageSource.getMessage(
            "exception.spring.noHandlerFound",
            new Object[]{"GET", "/noHandlerFound"},
            new Locale("en")
        ))
        .thenReturn("Mocked");

    RestAssuredMockMvc
        .given()
        .header("Accept-Language", "en")
        .when()
        .get("/noHandlerFound")
        .then()
        .status(HttpStatus.NOT_FOUND)
        .expect(ResultMatcher.matchAll(
            result -> Assertions
                .assertThat(result.getResolvedException())
                .isExactlyInstanceOf(NoHandlerFoundException.class)
                .hasFieldOrPropertyWithValue("getHttpMethod", "GET")
                .hasFieldOrPropertyWithValue("getRequestURL", "/noHandlerFound"),
            result -> Assertions
                .assertThat(result.getResponse())
                .hasFieldOrPropertyWithValue("getErrorMessage", "Mocked")
        ));
  }


  @Controller
  private static class TestController {

    @GetMapping("/httpRequestMethodNotSupported")
    private ResponseEntity<Object> httpRequestMethodNotSupported() {
      return ResponseEntity.ok("Get request passed");
    }

    @PostMapping(path = "/httpMediaTypeNotSupported", consumes = "application/json")
    private ResponseEntity<Object> httpMediaTypeNotSupported(@RequestBody Target target) {
      return ResponseEntity.ok("Request body passed: " + target);
    }

    @GetMapping(path = "/httpMediaTypeNotAcceptable", produces = "application/json")
    private ResponseEntity<Object> httpMediaTypeNotAcceptable() {
      return ResponseEntity.ok("Request passed");
    }

    @GetMapping("/missingPathVariable")
    private ResponseEntity<Object> httpMissingPathVariable(@PathVariable String var) {
      return ResponseEntity.ok("Path variable passed: " + var);
    }

    @GetMapping("/missingServletRequestParameter")
    private ResponseEntity<Object> missingServletRequestParameter(@RequestParam String param) {
      return ResponseEntity.ok("Request param passed: " + param);
    }

    @GetMapping("/servletRequestBinding")
    private ResponseEntity<Object> servletRequestBinding(@CookieValue String cookie) {
      return ResponseEntity.ok("Cookie value passed: " + cookie);
    }

    @PostMapping("/httpMessageNotReadable")
    private ResponseEntity<Object> httpMessageNotReadable(@RequestBody Target target) {
      return ResponseEntity.ok("Target passed " + target);
    }

    @GetMapping("/typeMismatch")
    private ResponseEntity<Object> typeMismatch(@RequestParam Integer intParam) {
      return ResponseEntity.ok("Param passed:" + intParam);
    }

    @PostMapping("/methodArgumentNotValid")
    private ResponseEntity<Object> methodArgumentNotValid(@Validated @RequestBody Target target) {
      return ResponseEntity.ok("Target valid: " + target);
    }

    @PostMapping("/missingServletRequestPart")
    private ResponseEntity<Object> missingServletRequestPart(@RequestPart String part) {
      return ResponseEntity.ok("Part passed: " + part);
    }

    @PostMapping("/bind")
    private ResponseEntity<Object> bind(@Validated @ModelAttribute Target target) {
      return ResponseEntity.ok("Target valid: " + target);
    }

  }

  private static class Target {

    private String notBlank;

    public String getNotBlank() {
      return notBlank;
    }

    public void setNotBlank(String notBlank) {
      this.notBlank = notBlank;
    }

  }

  private static class TargetValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
      return Target.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "notBlank", "code");
    }

  }

}
