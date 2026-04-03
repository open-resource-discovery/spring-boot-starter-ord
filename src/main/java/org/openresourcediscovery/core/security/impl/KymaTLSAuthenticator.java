package org.openresourcediscovery.core.security.impl;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.openresourcediscovery.utils.TLS.decode;
import static org.openresourcediscovery.utils.TLS.tokenize;
import static org.openresourcediscovery.utils.TLS.tokensMatch;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.security.TLSAuthenticator;

@RequiredArgsConstructor
public class KymaTLSAuthenticator implements TLSAuthenticator {

  private final Set<TrustedCertificate> trustedCertificates;

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    String[] issuer = tokenize(decode(request.getHeader("X-SSL-Client-Issuer")));
    String[] subject = tokenize(decode(request.getHeader("X-SSL-Client-CN")));

    return isNotEmpty(issuer)
        && isNotEmpty(subject)
        && trustedCertificates.stream().anyMatch(trusted -> {
          return (Objects.equals("*", trusted.issuer()) || tokensMatch(issuer, tokenize(trusted.issuer())))
              && (Objects.equals("*", trusted.subject())
                  || tokensMatch(subject, tokenize(trusted.subject())));
        });
  }
}
