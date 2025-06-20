package com.example.springframework.web.servlet.handler;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

public class LocalizedHandlerExceptionResolver extends DefaultHandlerExceptionResolver {

  protected final MessageSource messageSource;

  public LocalizedHandlerExceptionResolver(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Override
  protected ModelAndView handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.httpRequestMethodNotSupported",
        new Object[]{
            ex.getMethod()
        },
        request.getLocale()
    );
    String[] supportedMethods = ex.getSupportedMethods();

    // No statement found where can be null. May be absent by internal spring logic.

    if (supportedMethods != null) {
      response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods, ", "));
    }

    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.httpMediaTypeNotSupported",
        new Object[]{
            ex.getContentType()
        },
        request.getLocale()
    );
    List<MediaType> supportedMediaTypes = ex.getSupportedMediaTypes();

    // HttpMediaTypeException::getSupportedMediaTypes returns media type(s), specified
    // in @RequestMapping.consumes() property, if empty (default) - return all types.
    // List will never be empty.

    if (!CollectionUtils.isEmpty(supportedMediaTypes)) {
      response.setHeader("Accept", MediaType.toString(supportedMediaTypes));
    }

    response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleHttpMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.httpMediaTypeNotAcceptable",
        null,
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleMissingPathVariable(
      MissingPathVariableException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    this.sendServerError(ex, request, response);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.missingServletRequestParameter",
        new Object[]{
            ex.getParameterName()
        },
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleServletRequestBindingException(
      ServletRequestBindingException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.servletRequestBinding",
        null,
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleTypeMismatch(
      TypeMismatchException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.typeMismatch",
        null,
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.httpMessageNotReadable",
        null,
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    return this.handleBindException(
        new BindException(ex.getBindingResult()),
        request,
        response,
        handler
    );
  }

  @Override
  protected ModelAndView handleMissingServletRequestPartException(
      MissingServletRequestPartException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.missingServletRequestPart",
        new Object[]{
            ex.getRequestPartName()
        },
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleBindException(
      BindException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.bind",
        new Object[]{
            ex.getBindingResult().getErrorCount()
        },
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    return new ModelAndView();
  }

  @Override
  protected ModelAndView handleNoHandlerFoundException(
      NoHandlerFoundException ex,
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.noHandlerFound",
        new Object[]{
            ex.getHttpMethod(),
            ex.getRequestURL()
        },
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
    return new ModelAndView();
  }

  @Override
  protected void sendServerError(
      Exception ex,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws IOException {
    String message = messageSource.getMessage(
        "exception.spring.serverError",
        null,
        request.getLocale()
    );
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
  }

}
