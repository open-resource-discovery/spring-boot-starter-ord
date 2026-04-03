package org.openresourcediscovery.core.security;

import jakarta.servlet.http.HttpServletRequest;

public interface TLSAuthenticator {

  record TrustedCertificate(String issuer, String subject) {}

  boolean isAuthenticated(HttpServletRequest request);
}
