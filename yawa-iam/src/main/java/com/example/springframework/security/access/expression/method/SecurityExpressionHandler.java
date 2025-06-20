package com.example.springframework.security.access.expression.method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;

public class SecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

  private final ObjectProvider<SecurityExpressionRootExtended> expressionRootProvider;

  public SecurityExpressionHandler(
      ObjectProvider<SecurityExpressionRootExtended> expressionRootProvider
  ) {
    this.expressionRootProvider = expressionRootProvider;
  }

  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
      Authentication authentication,
      MethodInvocation invocation
  ) {
    SecurityExpressionRootExtended root = expressionRootProvider.getObject(authentication);
    root.setThis(invocation.getThis());
    return root;
  }

}
