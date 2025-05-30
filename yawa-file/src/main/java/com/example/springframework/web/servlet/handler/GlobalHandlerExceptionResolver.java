package com.example.springframework.web.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.ModelAndView;

import com.example.springframework.web.LocalizedException;

public class GlobalHandlerExceptionResolver extends LocalizedHandlerExceptionResolver {

  public GlobalHandlerExceptionResolver(MessageSource messageSource) {
    super(messageSource);
  }

  @Override
  public final ModelAndView doResolveException(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex
  ) {
    ModelAndView parentResolveResult = super.doResolveException(request, response, handler, ex);

    if (parentResolveResult != null) {
      return parentResolveResult;
    }

    try {
      if (ex instanceof LocalizedException) {
        return handleLocalized((LocalizedException) ex, request, response);
      }

      if (ex instanceof AccessDeniedException) {
        return handleAccessDenied((AccessDeniedException) ex, request, response);
      }

      if (ex instanceof AuthenticationException) {
        return handleAuthentication((AuthenticationException) ex, request, response);
      }

      super.sendServerError(ex, request, response);
    } catch (Exception e) {
      logger.error("Failed to handle exception", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    logger.error("Global exception", ex);
    return new ModelAndView();
  }

  private ModelAndView handleLocalized(
      LocalizedException ex,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws Exception {
    String message = messageSource.getMessage(ex, request.getLocale());
    response.sendError(ex.getStatusCode(), message);
    return new ModelAndView();
  }

  // Investigate security exception localization
  // See: org.springframework.security.core.SpringSecurityMessageSource

  public ModelAndView handleAccessDenied(
      AccessDeniedException ex,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws Exception {
    response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
    return new ModelAndView();
  }

  public ModelAndView handleAuthentication(
      AuthenticationException ex,
      HttpServletRequest request,
      HttpServletResponse response
  ) throws Exception {
    response.sendError(HttpStatus.FORBIDDEN.value(), "Access denied");
    return new ModelAndView();
  }

}
