package com.example.springframework.boot.web.servlet.error;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

public class LocalizedErrorAttributesTest {

  @Test
  public void givenTrueIncludeException_whenNoErrorAttribute_thenNullExceptionAttribute() {
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(null, true);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, false))
        .hasSize(7)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("exception", null)
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path");
  }

  @Test
  public void givenTrueIncludeException_thenAddExceptionAttribute() {
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(null, true);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(
        "org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
        new RuntimeException("Error")
    );
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, false))
        .hasSize(7)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("exception", "java.lang.RuntimeException")
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path");
  }

  @Test
  public void givenTrueIncludeStackTrace_whenNoErrorAttribute_thenNullTraceAttribute() {
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(null, false);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, true))
        .hasSize(7)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path")
        .containsEntry("trace", null);
  }

  @Test
  public void givenTrueIncludeStackTrace_thenAddTraceAttribute() {
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(null, false);
    RuntimeException exception = new RuntimeException("Error");
    exception.setStackTrace(new StackTraceElement[]{
        new StackTraceElement("Class", "method", "Class.java", 1)
    });
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(
        "org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
        exception
    );
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, true))
        .hasSize(7)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path")
        .containsEntry("trace", "java.lang.RuntimeException: Error" + System.lineSeparator()
            + "\tat Class.method(Class.java:1)" + System.lineSeparator());
  }

  @Test
  public void givenIllegalStatusCode_thenNullErrorAttribute() {
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(null, false);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute("javax.servlet.error.status_code", 999);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, false))
        .hasSize(6)
        .containsKey("timestamp")
        .containsEntry("status", 999)
        .containsEntry("error", null)
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path");
  }

  @Test
  public void givenBindException_whenNoFieldErrors_thenNullErrorsAttribute() {
    MessageSource messageSource = Mockito.mock(MessageSource.class);
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(messageSource, false);
    BindException bindException = new BindException(new Object(), "object");
    bindException.addError(new ObjectError("object", ""));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(
        "org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
        bindException
    );
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, false))
        .hasSize(6)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("message", "Message")
        .containsEntry("errors", null)
        .containsEntry("path", "/path");
  }

  @Test
  public void givenMethodArgumentNotValidException_thenAddErrorsAttribute() {
    MessageSource messageSource = Mockito.mock(MessageSource.class);
    Mockito
        .when(messageSource.getMessage(
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        ))
        .thenReturn("Mocked");
    ErrorAttributes errorAttributes = new LocalizedErrorAttributes(messageSource, false);
    BindingResult bindingResult = new BeanPropertyBindingResult(null, "object");
    bindingResult.addError(new FieldError("object", "field", ""));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(
        "org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR",
        new MethodArgumentNotValidException(null, bindingResult)
    );
    request.setAttribute("javax.servlet.error.status_code", 400);
    request.setAttribute("javax.servlet.error.message", "Message");
    request.setAttribute("javax.servlet.error.request_uri", "/path");
    ServletWebRequest webRequest = new ServletWebRequest(request);

    Assertions
        .assertThat(errorAttributes.getErrorAttributes(webRequest, false))
        .hasSize(6)
        .containsKey("timestamp")
        .containsEntry("status", 400)
        .containsEntry("error", "Bad Request")
        .containsEntry("message", "Message")
        .containsEntry("errors", Maps.newHashMap("field", Lists.newArrayList("Mocked")))
        .containsEntry("path", "/path");
  }

}
