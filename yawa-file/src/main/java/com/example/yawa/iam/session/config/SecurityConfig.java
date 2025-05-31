package com.example.yawa.iam.session.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;
import com.example.yawa.iam.session.expression.SessionExpressionRoot;

@Configuration("sessionSecurityConfig")
public class SecurityConfig {

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public SecurityExpressionRootExtended securityExpressionRoot(Authentication auth) {
    SecurityExpressionRootExtended sessionExpressionRoot = new SessionExpressionRoot(auth);
    sessionExpressionRoot.setTrustResolver(new AuthenticationTrustResolverImpl());
    sessionExpressionRoot.setPermissionEvaluator(new DenyAllPermissionEvaluator());
    return sessionExpressionRoot;
  }

}
