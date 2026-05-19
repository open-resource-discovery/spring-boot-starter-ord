package org.openresourcediscovery.core.security.impl;

import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.OPEN;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@RequiredArgsConstructor
public class OrdAuthorizationManagerImpl implements OrdAuthorizationManager {

  private final AccessStrategiesResolver accessStrategiesResolver;
  private final Collection<OrdAuthenticationManager> authenticators;

  @Override
  public AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context) {
    HttpServletRequest request = context.getRequest();

    return new AuthorizationDecision( //
        accessStrategiesResolver.resolve(request).contains(OPEN.getKey())
            || authenticators.stream().anyMatch(authenticator -> authenticator.isAuthenticated(request)));
  }
}
