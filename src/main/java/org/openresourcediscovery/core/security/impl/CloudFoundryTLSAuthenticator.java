package org.openresourcediscovery.core.security.impl;

import static java.util.Objects.nonNull;
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

public class CloudFoundryTLSAuthenticator implements OrdAuthenticationManager {

  private final AccessStrategiesResolver accessStrategiesResolver;
  private final Map<AccessStrategy, TrustConfiguration> trustedCertificates;

  public CloudFoundryTLSAuthenticator(AccessStrategiesResolver accessStrategiesResolver) {
    this.trustedCertificates = new ConcurrentHashMap<>();
    this.accessStrategiesResolver = accessStrategiesResolver;
  }

  public CloudFoundryTLSAuthenticator configure(
      AccessStrategy strategy,
      Set<String> trustedRootCertificateAuthorityDNs,
      Set<TrustedCertificate> trustedCertificates) {
    this.trustedCertificates.put(
        strategy,
        new TrustConfiguration(copyOf(trustedRootCertificateAuthorityDNs), copyOf(trustedCertificates)));

    return this;
  }

  @Override
  public boolean isAuthenticated(HttpServletRequest request) {
    String rootDN = request.getHeader("X-SSL-Client-Root-CA-DN");
    String issuerDN = request.getHeader("X-SSL-Client-Issuer-DN");
    String subjectDN = request.getHeader("X-SSL-Client-Subject-DN");
    Set<String> accessStrategies = accessStrategiesResolver.resolve(request);

    return isXfccProxyVerified(request)
        && trustedCertificates.keySet().stream()
            .filter(strategy -> accessStrategies.contains(strategy.getKey()))
            .anyMatch(strategy -> isTrustedRootCertificateAuthorityDN(strategy, rootDN)
                && isTrustedCertificate(strategy, issuerDN, subjectDN));
  }

  private boolean isXfccProxyVerified(HttpServletRequest request) {
    return nonNull(request.getHeader("X-Forwarded-Client-Cert"))
        && Objects.equals("1", request.getHeader("X-SSL-Client"))
        && Objects.equals("0", request.getHeader("X-SSL-Client-Verify"));
  }

  private boolean isTrustedCertificate(AccessStrategy strategy, String issuer, String subject) {
    String[] issuerTokens = tokenize(decode(issuer));
    String[] subjectTokens = tokenize(decode(subject));

    return isNotEmpty(issuerTokens)
        && isNotEmpty(subjectTokens)
        && trustedCertificates.get(strategy).trustedCertificates().stream()
            .anyMatch(trusted -> {
              return (Objects.equals("*", trusted.issuer())
                      || tokensMatch(issuerTokens, tokenize(trusted.issuer())))
                  && (Objects.equals("*", trusted.subject())
                      || tokensMatch(subjectTokens, tokenize(trusted.subject())));
            });
  }

  private boolean isTrustedRootCertificateAuthorityDN(AccessStrategy strategy, String rootCertificateAuthorityDN) {
    String[] tokens = tokenize(decode(rootCertificateAuthorityDN));

    return isNotEmpty(tokens)
        && trustedCertificates.get(strategy).trustedRootCertificateAuthorityDNs().stream()
            .anyMatch(trusted -> tokensMatch(tokens, tokenize(trusted)));
  }

  private record TrustConfiguration(
      Set<String> trustedRootCertificateAuthorityDNs, Set<TrustedCertificate> trustedCertificates) {}
}
