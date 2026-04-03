package org.openresourcediscovery.api.controllers;

import static org.springframework.http.CacheControl.noCache;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.OrdAuthorizationManager;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenResourceDiscoveryController {

  private static final Set<String> PUBLIC_ONLY = Set.of("public");
  private static final Set<String> ALL = Set.of("public", "internal", "private");

  private final DocumentSchemaRegistry documentSchemaRegistry;
  private final OrdAuthorizationManager ordAuthorizationManager;

  @GetMapping("/ord/v1/documents/{id}")
  public ResponseEntity<DocumentSchema> getOrdDocument(HttpServletRequest request, @PathVariable("id") String id) {
    return ResponseEntity.ok()
        .cacheControl(noCache().mustRevalidate())
        .header("X-Powered-By", "ORD Java Spring Boot Starter")
        .body(documentSchemaRegistry
            .lookupDocumentSchema(id, ordAuthorizationManager.isAuthenticated(request) ? ALL : PUBLIC_ONLY)
            .orElseThrow(() -> new NoSuchElementException("Document not found: " + id)));
  }
}
