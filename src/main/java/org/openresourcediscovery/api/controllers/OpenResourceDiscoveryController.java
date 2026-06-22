package org.openresourcediscovery.api.controllers;

import static org.springframework.http.CacheControl.noCache;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry.StaticResource;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.core.io.Resource;
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
  private final StaticResourceRegistry staticResourceRegistry;
  private final Optional<OrdAuthenticationManager> ordAuthenticationManager;

  @GetMapping("${ord.api-base-path-documents:/ord/v1/documents}/{name}")
  public ResponseEntity<DocumentSchema> getOrdDocument(
      HttpServletRequest request, @PathVariable("name") String name) {
    return ResponseEntity.ok()
        .contentType(APPLICATION_JSON)
        .cacheControl(noCache().mustRevalidate())
        .header("X-Powered-By", "ORD Java Spring Boot Starter")
        .body(documentSchemaRegistry
            .lookupDocumentSchema(name, isAuthenticated(request) ? ALL : PUBLIC_ONLY)
            .orElseThrow(() -> new NoSuchElementException("Document not found: " + name)));
  }

  @SneakyThrows
  @GetMapping("${ord.api-base-path-resources:/ord/v1/resources}/{name}")
  public ResponseEntity<Resource> getOrdResource(@PathVariable("name") String name) {
    StaticResource staticResource = staticResourceRegistry
        .lookupStaticResource(name)
        .orElseThrow(() -> new NoSuchElementException("Resource not found: " + name));

    return ResponseEntity.ok()
        .contentType(staticResource.mediaType())
        .cacheControl(noCache().mustRevalidate())
        .header("X-Powered-By", "ORD Java Spring Boot Starter")
        .body(staticResource.resource());
  }

  private boolean isAuthenticated(HttpServletRequest request) {
    return ordAuthenticationManager.map(oam -> oam.isAuthenticated(request)).orElse(false);
  }
}
