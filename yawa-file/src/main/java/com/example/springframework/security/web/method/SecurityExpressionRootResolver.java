package com.example.springframework.security.web.method;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class SecurityExpressionRootResolver implements HandlerMethodArgumentResolver {

  private final ObjectProvider<SecurityExpressionOperations> securityExpressionRootProvider;

  public SecurityExpressionRootResolver(ObjectProvider<SecurityExpressionOperations> provider) {
    this.securityExpressionRootProvider = provider;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return SecurityExpressionOperations.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory
  ) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return securityExpressionRootProvider.getObject(authentication);
  }

}
