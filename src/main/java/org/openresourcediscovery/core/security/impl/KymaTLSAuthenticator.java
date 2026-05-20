package org.openresourcediscovery.core.security.impl;

import static java.util.Set.copyOf;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.openresourcediscovery.utils.TLS.decode;
import static org.openresourcediscovery.utils.TLS.tokenize;
import static org.openresourcediscovery.utils.TLS.tokensMatch;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;

public class KymaTLSAuthenticator implements OrdAuthenticationManager {

  private final AccessStrategiesResolver accessStrategiesResolver;
  private final Map<AccessStrategy, Set<TrustedCertificate>> trustedCertificates;

  public KymaTLSAuthenticator(AccessStrategiesResolver accessStrategiesResolver) {
    this.trustedCertificates = new ConcurrentHashMap<>();
    this.accessStrategiesResolver = accessStrategiesResolver;
  }

  public KymaTLSAuthenticator configure(AccessStrategy strategy, Set<TrustedCertificate> trustedCertificates) {
    this.trustedCertificates.put(strategy, copyOf(trustedCertificates));

    return this;
  }

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    Set<String> accessStrategies = accessStrategiesResolver.resolve(request);
    String[] subject = tokenize(decode(request.getHeader("X-SSL-Client-CN")));
    String[] issuer = tokenize(decode(request.getHeader("X-SSL-Client-Issuer")));

    return isNotEmpty(issuer)
        && isNotEmpty(subject)
        && trustedCertificates.keySet().stream()
            .filter(strategy -> accessStrategies.contains(strategy.getKey()))
            .map(trustedCertificates::get)
            .flatMap(Set::stream)
            .anyMatch(certificate -> {
              return (Objects.equals("*", certificate.issuer())
                      || tokensMatch(issuer, tokenize(certificate.issuer())))
                  && (Objects.equals("*", certificate.subject())
                      || tokensMatch(subject, tokenize(certificate.subject())));
            });
  }
}
