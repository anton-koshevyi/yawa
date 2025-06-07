package com.example.yawa.application.config;

import java.security.Security;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.springframework.security.access.expression.SecurityExpressionRootExtended;
import com.example.springframework.security.access.expression.method.SecurityExpressionHandler;
import com.example.yawa.application.filter.AuthorizationFilter;
import com.example.yawa.application.filter.SnsSignatureFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final HandlerExceptionResolver handlerExceptionResolver;
  private final AuthorizationFilter authorizationFilter;
  private final SnsSignatureFilter snsSignatureFilter;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Autowired
  public SecurityConfig(
      HandlerExceptionResolver handlerExceptionResolver,
      AuthorizationFilter authorizationFilter,
      SnsSignatureFilter snsSignatureFilter
  ) {
    super(true);
    this.handlerExceptionResolver = handlerExceptionResolver;
    this.authorizationFilter = authorizationFilter;
    this.snsSignatureFilter = snsSignatureFilter;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .servletApi().and()
        .anonymous().and()
        .exceptionHandling(c -> c
            .authenticationEntryPoint(this::forwardExceptionHandling)
            .accessDeniedHandler(this::forwardExceptionHandling))
        .addFilterAfter(snsSignatureFilter, SecurityContextHolderAwareRequestFilter.class)
        .addFilterAfter(authorizationFilter, SecurityContextHolderAwareRequestFilter.class);
  }

  private void forwardExceptionHandling(
      HttpServletRequest request,
      HttpServletResponse response,
      Exception exception
  ) {
    handlerExceptionResolver.resolveException(request, response, null, exception);
  }


  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final ObjectProvider<SecurityExpressionRootExtended> expressionRootProvider;
    private final ApplicationContext applicationContext;

    @Autowired
    public MethodSecurityConfig(
        ObjectProvider<SecurityExpressionRootExtended> expressionRootProvider,
        ApplicationContext applicationContext
    ) {
      this.expressionRootProvider = expressionRootProvider;
      this.applicationContext = applicationContext;
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
      SecurityExpressionHandler expressionHandler =
          new SecurityExpressionHandler(expressionRootProvider);
      expressionHandler.setApplicationContext(applicationContext);
      return expressionHandler;
    }

  }

}
