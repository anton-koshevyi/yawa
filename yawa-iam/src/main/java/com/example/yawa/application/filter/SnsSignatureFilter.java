package com.example.yawa.application.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.sns.message.SnsMessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.springframework.web.util.CachedBodyRequestWrapper;
import com.example.yawa.application.exception.AuthenticationException;

@Component
public class SnsSignatureFilter extends OncePerRequestFilter {

  private final SnsMessageManager snsMessageManager;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Autowired
  public SnsSignatureFilter(
      SnsMessageManager snsMessageManager,
      HandlerExceptionResolver handlerExceptionResolver
  ) {
    this.snsMessageManager = snsMessageManager;
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {
    String header = request.getHeader("x-amz-sns-message-type");

    if (header == null) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest cachedRequest = new CachedBodyRequestWrapper(request);

    try {
      snsMessageManager.parseMessage(cachedRequest.getInputStream());
      chain.doFilter(cachedRequest, response);
    } catch (SdkClientException e) {
      AuthenticationException authException = new AuthenticationSnsBySignatureException(e);
      handlerExceptionResolver.resolveException(cachedRequest, response, null, authException);
    }
  }

  private static class AuthenticationSnsBySignatureException extends AuthenticationException {

    AuthenticationSnsBySignatureException(Throwable cause) {
      super("exception.authentication.sns.bySignature", cause);
    }

  }

}
