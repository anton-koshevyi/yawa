package com.example.springframework.security.access.expression;

import java.util.UUID;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public abstract class SecurityExpressionRootExtended
    extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

  private final MethodSecurityExpressionRoot methodSecurityExpressionRoot;

  protected SecurityExpressionRootExtended(Authentication auth) {
    super(auth);
    this.methodSecurityExpressionRoot = new MethodSecurityExpressionRoot(auth);
  }

  public abstract boolean isUser(UUID userId);

  public abstract UUID getUserId();

  public abstract boolean isSession(UUID sessionId);

  public abstract UUID getSessionId();

  @Override
  public final void setFilterObject(Object filterObject) {
    methodSecurityExpressionRoot.setFilterObject(filterObject);
  }

  @Override
  public final Object getFilterObject() {
    return methodSecurityExpressionRoot.getFilterObject();
  }

  @Override
  public final void setReturnObject(Object returnObject) {
    methodSecurityExpressionRoot.setReturnObject(returnObject);
  }

  @Override
  public final Object getReturnObject() {
    return methodSecurityExpressionRoot.getReturnObject();
  }

  @Override
  public final Object getThis() {
    return methodSecurityExpressionRoot.getThis();
  }

  public void setThis(Object target) {
    methodSecurityExpressionRoot.setThis(target);
  }


  /**
   * Obtained from native Spring implementation of {@link MethodSecurityExpressionOperations}.
   * Copy-pasted due to access limitation of native implementation.
   *
   * @see org.springframework.security.access.expression.method.MethodSecurityExpressionRoot
   */
  private static class MethodSecurityExpressionRoot
      extends SecurityExpressionRoot
      implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;

    MethodSecurityExpressionRoot(Authentication auth) {
      super(auth);
    }

    @Override
    public void setFilterObject(Object filterObject) {
      this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
      return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
      this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
      return returnObject;
    }

    @Override
    public Object getThis() {
      return target;
    }

    /**
     * Sets the "this" property for use in expressions. Typically this will be the "this"
     * property of the {@code JoinPoint} representing the method invocation which is being
     * protected.
     *
     * @param target the target object on which the method in is being invoked.
     */
    public void setThis(Object target) {
      this.target = target;
    }

  }

}
