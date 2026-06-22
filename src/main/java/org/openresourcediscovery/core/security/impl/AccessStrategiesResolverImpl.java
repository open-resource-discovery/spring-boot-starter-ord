package org.openresourcediscovery.core.security.impl;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@RequiredArgsConstructor
public class AccessStrategiesResolverImpl implements AccessStrategiesResolver {

  private static final PathPatternRouteMatcher PATH_PATTERN_ROUTE_MATCHER =
      new PathPatternRouteMatcher(new PathPatternParser());

  private final OrdProperties ordProperties;
  private final DocumentSchemaRegistry documentSchemaRegistry;
  private final StaticResourceRegistry staticResourceRegistry;

  @Override
  public Set<String> resolve(HttpServletRequest request) {
    String path = defaultString(firstNonBlank(request.getPathInfo(), request.getServletPath()));
    RouteMatcher.Route route = PATH_PATTERN_ROUTE_MATCHER.parseRoute(path);

    if (PATH_PATTERN_ROUTE_MATCHER.match("%s/{name}".formatted(ordProperties.getDocumentsPath()), route)) {
      return extractDocumentAccessStrategies(route);
    }

    if (PATH_PATTERN_ROUTE_MATCHER.match("%s/{name}".formatted(ordProperties.getResourcesPath()), route)) {
      return extractStaticResourceAccessStrategies(route);
    }

    return emptySet();
  }

  private Set<String> extractDocumentAccessStrategies(RouteMatcher.Route route) {
    return Optional.ofNullable(PATH_PATTERN_ROUTE_MATCHER.matchAndExtract(
            "%s/{name}".formatted(ordProperties.getDocumentsPath()), route))
        .map(p -> p.get("name"))
        .map(documentSchemaRegistry::lookupAccessStrategies)
        .orElse(Set.of());
  }

  private Set<String> extractStaticResourceAccessStrategies(RouteMatcher.Route route) {
    return Optional.ofNullable(PATH_PATTERN_ROUTE_MATCHER.matchAndExtract(
            "%s/{name}".formatted(ordProperties.getResourcesPath()), route))
        .map(p -> p.get("name"))
        .map(staticResourceRegistry::lookupAccessStrategies)
        .orElse(Set.of());
  }
}
