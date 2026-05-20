package org.openresourcediscovery.core.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface OrdAuthenticationManager {

  @Getter
  @RequiredArgsConstructor
  enum AccessStrategy {
    OPEN("open"),
    BASIC("basic-auth"),
    CMP_MTLS("sap:cmp-mtls:v1"),
    BAH_MTLS("sap.businesshub:mtls:v1");

    private final String key;
  }

  record TrustedCertificate(String issuer, String subject) {}

  boolean isAuthenticated(HttpServletRequest request);
}
