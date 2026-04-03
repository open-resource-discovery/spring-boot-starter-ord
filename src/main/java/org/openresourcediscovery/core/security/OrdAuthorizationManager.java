package org.openresourcediscovery.core.security;

import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public interface OrdAuthorizationManager extends AuthorizationManager<RequestAuthorizationContext> {

  @Override
  AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context);
}
