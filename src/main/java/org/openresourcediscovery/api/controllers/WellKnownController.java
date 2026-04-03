package org.openresourcediscovery.api.controllers;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.springframework.http.CacheControl.noCache;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WellKnownController {

  private final DocumentSchemaRegistry documentSchemaRegistry;

  @GetMapping("/.well-known/open-resource-discovery")
  public ResponseEntity<Map<String, Object>> getWellKnownDiscovery() {
    return ResponseEntity.ok()
        .cacheControl(noCache().mustRevalidate())
        .header("X-Powered-By", "ORD Java Spring Boot Starter")
        .body(Map.of("openResourceDiscoveryV1", Map.of("documents", asDocuments())));
  }

  private List<Map<String, Object>> asDocuments() {
    return documentSchemaRegistry.getAllDocumentIds().stream()
        .map(id -> ofEntries(
            entry("url", "/ord/v1/documents/%s".formatted(id)),
            entry(
                "accessStrategies",
                documentSchemaRegistry.lookupAccessStrategies(id).stream()
                    .map(as -> Map.of("type", as))
                    .toList())))
        .toList();
  }
}
