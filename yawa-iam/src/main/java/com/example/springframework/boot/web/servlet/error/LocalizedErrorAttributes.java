package com.example.springframework.boot.web.servlet.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * Based on {@link DefaultErrorAttributes}, but with processing
 * binding errors without disclosing implementation details.
 * Private methods were made protected for ability to extension.
 */
public class LocalizedErrorAttributes extends DefaultErrorAttributes {

  private final MessageSource messageSource;
  private final boolean includeException;

  public LocalizedErrorAttributes(MessageSource messageSource, boolean includeException) {
    this.messageSource = messageSource;
    this.includeException = includeException;
  }

  @Override
  public final Map<String, Object> getErrorAttributes(
      WebRequest request,
      boolean includeStackTrace
  ) {
    Map<String, Object> errorAttributes = new LinkedHashMap<>();

    errorAttributes.put("timestamp", new Date());
    errorAttributes.put("status", this.getStatus(request));
    errorAttributes.put("error", this.getReasonPhrase(request));

    if (includeException) {
      errorAttributes.put("exception", this.getErrorName(request));
    }

    errorAttributes.put("message", this.getMessage(request));
    errorAttributes.put("errors", this.getBindingErrors(request));
    errorAttributes.put("path", this.getPath(request));

    if (includeStackTrace) {
      errorAttributes.put("trace", this.getStackTrace(request));
    }

    return errorAttributes;
  }

  /**
   * Parent implementation of method processes error before any handler
   * (because has {@link org.springframework.core.Ordered#HIGHEST_PRECEDENCE}
   * and set own attribute to request. Then, during processing by
   * {@link org.springframework.boot.web.servlet.error.ErrorController},
   * extracts attribute and read original exception details. <br>
   * As an alternative decision - add own public constant of request
   * error attribute, set exception from handler to it and then read
   * from request instead of {@code javax.servlet.error.exception}
   */
  @Override
  public final Throwable getError(WebRequest request) {
    return super.getError(request);
  }

  protected Integer getStatus(WebRequest request) {
    return this.getAttribute(request, "javax.servlet.error.status_code");
  }

  protected String getReasonPhrase(WebRequest request) {
    Integer status = this.getAttribute(request, "javax.servlet.error.status_code");

    try {
      return HttpStatus.valueOf(status).getReasonPhrase();
    } catch (Exception ex) {
      return null;
    }
  }

  protected String getErrorName(WebRequest request) {
    Throwable error = this.getError(request);

    if (error == null) {
      return null;
    }

    return error.getClass().getName();
  }

  protected Map<String, List<String>> getBindingErrors(WebRequest request) {
    Throwable error = this.getError(request);
    BindingResult bindingResult;

    if (error instanceof BindingResult) {
      bindingResult = (BindingResult) error;
    } else if (error instanceof MethodArgumentNotValidException) {
      bindingResult = ((MethodArgumentNotValidException) error).getBindingResult();
    } else {
      return null;
    }

    if (!bindingResult.hasFieldErrors()) {
      return null;
    }

    Locale locale = request.getLocale();
    return bindingResult.getFieldErrors()
        .stream()
        .map(FieldError::getField)
        .distinct()
        .collect(Collectors.toMap(
            Function.identity(),
            field -> bindingResult.getFieldErrors(field)
                .stream()
                .map(e -> messageSource.getMessage(e, locale))
                .collect(Collectors.toList())
        ));
  }

  protected String getMessage(WebRequest request) {
    return this.getAttribute(request, "javax.servlet.error.message");
  }

  protected String getStackTrace(WebRequest request) {
    Throwable error = this.getError(request);

    if (error == null) {
      return null;
    }

    StringWriter stackTrace = new StringWriter();
    error.printStackTrace(new PrintWriter(stackTrace));
    stackTrace.flush();
    return stackTrace.toString();
  }

  protected String getPath(WebRequest request) {
    return this.getAttribute(request, "javax.servlet.error.request_uri");
  }

  protected final <T> T getAttribute(WebRequest request, String name) {
    return (T) request.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

}
