package org.openresourcediscovery.core.security;

import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public interface OrdAuthorizationManager extends AuthorizationManager<RequestAuthorizationContext> {

  @Override
  AuthorizationResult authorize(Supplier<? extends Authentication> supplier, RequestAuthorizationContext context);
}
