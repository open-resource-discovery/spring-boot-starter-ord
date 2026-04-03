package org.openresourcediscovery.core.services;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public interface OrdAuthorizationManager extends AuthorizationManager<RequestAuthorizationContext> {

  boolean isAuthenticated(HttpServletRequest request);

  @Override
  AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context);
}
