package org.openresourcediscovery.core.security.impl;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.springframework.security.authentication.AuthenticationTrustResolver;

@RequiredArgsConstructor
public class OrdAuthenticationManagerImpl implements OrdAuthenticationManager {

  private final TLSAuthenticator tlsAuthenticator;
  private final AuthenticationTrustResolver authenticationTrustResolver;

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    return tlsAuthenticator.isAuthenticated(request)
        || authenticationTrustResolver.isAuthenticated(getContext().getAuthentication());
  }
}
