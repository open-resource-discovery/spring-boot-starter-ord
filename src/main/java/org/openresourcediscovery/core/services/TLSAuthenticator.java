package org.openresourcediscovery.core.services;

import jakarta.servlet.http.HttpServletRequest;

public interface TLSAuthenticator {

  record TrustedCertificate(String issuer, String subject) {}

  boolean isAuthenticated(HttpServletRequest request);
}
