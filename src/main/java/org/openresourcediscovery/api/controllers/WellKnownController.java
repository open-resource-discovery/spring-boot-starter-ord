package org.openresourcediscovery.api.controllers;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;
import static org.springframework.http.CacheControl.noCache;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WellKnownController {

  private static final String DEFAULT_PERSPECTIVE = "system-instance";

  private final OrdProperties ordProperties;
  private final DocumentSchemaRegistry documentSchemaRegistry;

  @GetMapping("${ord.well-known-path:/.well-known/open-resource-discovery}")
  public ResponseEntity<Map<String, Object>> getWellKnownDiscovery() {
    return ResponseEntity.ok()
        .cacheControl(noCache().mustRevalidate())
        .header("X-Powered-By", "ORD Java Spring Boot Starter")
        .body(Map.of("openResourceDiscoveryV1", Map.of("documents", asDocuments())));
  }

  private List<Map<String, Object>> asDocuments() {
    return documentSchemaRegistry.getAllDocumentSchemas().entrySet().stream()
        .map(e -> ofEntries(
            entry("url", "%s/%s".formatted(ordProperties.getDocumentsPath(), e.getKey())),
            entry("perspective", firstNonBlank(e.getValue().getPerspective(), DEFAULT_PERSPECTIVE)),
            entry(
                "accessStrategies",
                documentSchemaRegistry.lookupAccessStrategies(e.getKey()).stream()
                    .map(as -> Map.of("type", as))
                    .toList())))
        .toList();
  }
}
