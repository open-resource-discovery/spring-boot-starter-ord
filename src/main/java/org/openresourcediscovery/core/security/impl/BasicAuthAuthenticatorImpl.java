package org.openresourcediscovery.core.security.impl;

import static org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy.BASIC;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;

@RequiredArgsConstructor
public class BasicAuthAuthenticatorImpl implements OrdAuthenticationManager {

  private final AccessStrategiesResolver accessStrategiesResolver;
  private final AuthenticationTrustResolver authenticationTrustResolver;

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    return accessStrategiesResolver.resolve(request).contains(BASIC.getKey())
        && authenticationTrustResolver.isFullyAuthenticated(getContext().getAuthentication());
  }
}
