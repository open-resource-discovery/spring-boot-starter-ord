package org.openresourcediscovery.core.security.impl;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@RequiredArgsConstructor
public class OrdAuthorizationManagerImpl implements OrdAuthorizationManager {

  private static final String ACCESS_STRATEGY_OPEN = "open";
  private static final String ACCESS_STRATEGY_BASIC_AUTH = "basic-auth";
  private static final String ACCESS_STRATEGY_CMP_MTLS = "sap:cmp-mtls:v1";
  private static final String PATH_PATTERN_ORD_DOCUMENT = "/ord/v1/documents/{id}";
  private static final PathPatternRouteMatcher PATH_PATTERN_ROUTE_MATCHER = new PathPatternRouteMatcher();

  private final TLSAuthenticator tlsAuthenticator;
  private final DocumentSchemaRegistry documentSchemaRegistry;
  private final AuthenticationTrustResolver authenticationTrustResolver;

  @Override
  public AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context) {
    Set<String> accessStrategies = extractDocumentId(context.getRequest())
        .map(documentSchemaRegistry::lookupAccessStrategies)
        .orElse(Set.of());

    return new AuthorizationDecision( //
        accessStrategies.contains(ACCESS_STRATEGY_OPEN)
            || (accessStrategies.contains(ACCESS_STRATEGY_CMP_MTLS)
                && tlsAuthenticator.isAuthenticated(context.getRequest()))
            || (accessStrategies.contains(ACCESS_STRATEGY_BASIC_AUTH)
                && authenticationTrustResolver.isAuthenticated(supplier.get())));
  }

  private static Optional<String> extractDocumentId(HttpServletRequest request) {
    return Optional.ofNullable(PATH_PATTERN_ROUTE_MATCHER.matchAndExtract(
            PATH_PATTERN_ORD_DOCUMENT,
            PATH_PATTERN_ROUTE_MATCHER.parseRoute(
                defaultString(firstNonBlank(request.getPathInfo(), request.getServletPath())))))
        .map(p -> p.get("id"));
  }
}
