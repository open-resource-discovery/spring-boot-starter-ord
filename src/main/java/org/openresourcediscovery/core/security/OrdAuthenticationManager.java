package org.openresourcediscovery.core.security;

import jakarta.servlet.http.HttpServletRequest;

public interface OrdAuthenticationManager {

  boolean isAuthenticated(HttpServletRequest request);
}
