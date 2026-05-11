package org.openresourcediscovery.core.security.impl;

import static java.util.Collections.emptySet;
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
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.RouteMatcher.Route;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@RequiredArgsConstructor
public class OrdAuthorizationManagerImpl implements OrdAuthorizationManager {

  private static final String ACCESS_STRATEGY_OPEN = "open";
  private static final String ACCESS_STRATEGY_BASIC_AUTH = "basic-auth";
  private static final String ACCESS_STRATEGY_CMP_MTLS = "sap:cmp-mtls:v1";
  private static final String PATH_PATTERN_ORD_DOCUMENT = "/ord/v1/documents/{name}";
  private static final String PATH_PATTERN_ORD_RESOURCE = "/ord/v1/resources/{name}";
  private static final PathPatternRouteMatcher PATH_PATTERN_ROUTE_MATCHER =
      new PathPatternRouteMatcher(new PathPatternParser());

  private final TLSAuthenticator tlsAuthenticator;
  private final DocumentSchemaRegistry documentSchemaRegistry;
  private final StaticResourceRegistry staticResourceRegistry;
  private final AuthenticationTrustResolver authenticationTrustResolver;

  @Override
  public AuthorizationDecision check(Supplier<Authentication> supplier, RequestAuthorizationContext context) {
    Set<String> accessStrategies = resolveAccessStrategies(context.getRequest());

    return new AuthorizationDecision( //
        accessStrategies.contains(ACCESS_STRATEGY_OPEN)
            || (accessStrategies.contains(ACCESS_STRATEGY_CMP_MTLS)
                && tlsAuthenticator.isAuthenticated(context.getRequest()))
            || (accessStrategies.contains(ACCESS_STRATEGY_BASIC_AUTH)
                && authenticationTrustResolver.isAuthenticated(supplier.get())));
  }

  private Set<String> resolveAccessStrategies(HttpServletRequest request) {
    String path = defaultString(firstNonBlank(request.getPathInfo(), request.getServletPath()));
    Route route = PATH_PATTERN_ROUTE_MATCHER.parseRoute(path);

    if (PATH_PATTERN_ROUTE_MATCHER.match(PATH_PATTERN_ORD_DOCUMENT, route)) {
      return extractDocumentAccessStrategies(route);
    }

    if (PATH_PATTERN_ROUTE_MATCHER.match(PATH_PATTERN_ORD_RESOURCE, route)) {
      return extractStaticResourceAccessStrategies(route);
    }

    return emptySet();
  }

  private Set<String> extractDocumentAccessStrategies(Route route) {
    return Optional.ofNullable(PATH_PATTERN_ROUTE_MATCHER.matchAndExtract(PATH_PATTERN_ORD_DOCUMENT, route))
        .map(p -> p.get("name"))
        .map(documentSchemaRegistry::lookupAccessStrategies)
        .orElse(Set.of());
  }

  private Set<String> extractStaticResourceAccessStrategies(Route route) {
    return Optional.ofNullable(PATH_PATTERN_ROUTE_MATCHER.matchAndExtract(PATH_PATTERN_ORD_RESOURCE, route))
        .map(p -> p.get("name"))
        .map(staticResourceRegistry::lookupAccessStrategies)
        .orElse(Set.of());
  }
}
