package com.example.yawa.application.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.springframework.boot.web.servlet.error.LocalizedErrorAttributes;
import com.example.springframework.security.web.method.SecurityExpressionRootResolver;
import com.example.springframework.web.servlet.handler.GlobalHandlerExceptionResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final MessageSource messageSource;
  private final boolean includeException;
  private final ObjectProvider<SecurityExpressionOperations> securityExpressionRootProvider;

  @Autowired
  public WebConfig(
      MessageSource messageSource,
      Environment env,
      ObjectProvider<SecurityExpressionOperations> securityExpressionRootProvider
  ) {
    this.messageSource = messageSource;
    this.includeException = env.getProperty(
        "server.error.include-exception",
        Boolean.class,
        false
    );
    this.securityExpressionRootProvider = securityExpressionRootProvider;
  }

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder
        .modulesToInstall(new JavaTimeModule())
        .build();
  }

  @Bean
  public LocalizedErrorAttributes errorAttributes() {
    return new LocalizedErrorAttributes(messageSource, includeException);
  }

  @Bean
  @Profile("development")
  public CommonsRequestLoggingFilter loggingFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeClientInfo(true);
    filter.setIncludePayload(true);
    return filter;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new SecurityExpressionRootResolver(securityExpressionRootProvider));
  }

  @Override
  public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    resolvers.add(errorAttributes()); // Highest order to put error into request attribute
    resolvers.add(new GlobalHandlerExceptionResolver(messageSource));
  }

}
