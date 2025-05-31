package com.example.yawa.iam.session.filter;

import java.io.IOException;
import java.security.Key;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.yawa.application.exception.ExpiredException;
import com.example.yawa.application.filter.AuthorizationFilter;
import com.example.yawa.iam.session.dto.SessionPrincipal;
import com.example.yawa.iam.session.exception.ExpiredSessionAccessTokenException;
import com.example.yawa.iam.session.repository.KeyRepository;
import com.example.yawa.iam.session.util.SessionAccessTokenClaims;

@Component
public class SessionAccessTokenFilter extends AuthorizationFilter {

  private static final String TOKEN_TYPE_PREFIX = "Bearer";

  private final KeyRepository keyRepository;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Autowired
  public SessionAccessTokenFilter(
      KeyRepository keyRepository,
      HandlerExceptionResolver handlerExceptionResolver
  ) {
    this.keyRepository = keyRepository;
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith(TOKEN_TYPE_PREFIX)) {
      String token = authHeader.substring(TOKEN_TYPE_PREFIX.length()).trim();

      try {
        Key signingKey = getAccessTokenSigningKey();
        Authentication authentication = new SessionAccessTokenAuthentication(token, signingKey);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException e) {
        ExpiredException expiredException = new ExpiredSessionAccessTokenException(token, e);
        handlerExceptionResolver.resolveException(request, response, null, expiredException);
        return;
      } catch (Exception e) {
        handlerExceptionResolver.resolveException(request, response, null, e);
        return;
      }
    }

    chain.doFilter(request, response);
  }

  private Key getAccessTokenSigningKey() {
    return keyRepository.findSessionAccess()
        .orElseThrow(() -> new NoSuchElementException(
            "No signing key for session access token"));
  }


  private static final class SessionAccessTokenAuthentication implements Authentication {

    private final Claims claims;

    private SessionAccessTokenAuthentication(String accessToken, Key signingKey) {
      this.claims = Jwts.parser()
          .setSigningKey(signingKey)
          .parseClaimsJws(accessToken)
          .getBody();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return AuthorityUtils.commaSeparatedStringToAuthorityList("");
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      String userId = claims.getSubject();
      String sessionId = claims.get(SessionAccessTokenClaims.SESSION_ID, String.class);

      return new SessionPrincipal(UUID.fromString(sessionId), UUID.fromString(userId));
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      throw new IllegalArgumentException("Unexpected invocation");
    }

    @Override
    public String getName() {
      return "sessionAccessToken";
    }

  }

}
