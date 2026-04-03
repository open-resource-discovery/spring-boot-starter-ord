package org.openresourcediscovery.core.services.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.openresourcediscovery.utils.TLS.decode;
import static org.openresourcediscovery.utils.TLS.tokenize;
import static org.openresourcediscovery.utils.TLS.tokensMatch;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.services.TLSAuthenticator;

@RequiredArgsConstructor
public class CloudFoundryTLSAuthenticator implements TLSAuthenticator {

  private final Set<TrustedCertificate> trustedCertificates;
  private final Set<String> trustedRootCertificateAuthorityDNs;

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    return isXfccProxyVerified(request)
        && isTrustedRootCertificateAuthorityDN(request.getHeader("X-SSL-Client-Root-CA-DN"))
        && isTrustedCertificate(
            request.getHeader("X-SSL-Client-Issuer-DN"), request.getHeader("X-SSL-Client-Subject-DN"));
  }

  private boolean isXfccProxyVerified(HttpServletRequest request) {
    return nonNull(request.getHeader("X-Forwarded-Client-Cert"))
        && Objects.equals("1", request.getHeader("X-SSL-Client"))
        && Objects.equals("0", request.getHeader("X-SSL-Client-Verify"));
  }

  private boolean isTrustedCertificate(String issuer, String subject) {
    String[] issuerTokens = tokenize(decode(issuer));
    String[] subjectTokens = tokenize(decode(subject));

    return isNotEmpty(issuerTokens)
        && isNotEmpty(subjectTokens)
        && trustedCertificates.stream().anyMatch(trusted -> {
          return (Objects.equals("*", trusted.issuer())
                  || tokensMatch(issuerTokens, tokenize(trusted.issuer())))
              && (Objects.equals("*", trusted.subject())
                  || tokensMatch(subjectTokens, tokenize(trusted.subject())));
        });
  }

  private boolean isTrustedRootCertificateAuthorityDN(String rootCertificateAuthorityDN) {
    String[] tokens = tokenize(decode(rootCertificateAuthorityDN));

    return isNotEmpty(tokens)
        && trustedRootCertificateAuthorityDNs.stream().anyMatch(trusted -> {
          return Objects.equals("*", trusted) || tokensMatch(tokens, tokenize(trusted));
        });
  }
}
