package org.openresourcediscovery.api;

import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.impl.DocumentSchemaRegistryImpl;
import org.openresourcediscovery.model.DocumentSchema;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

class OrdDocumentServiceSnapshotTest {

  private ObjectMapper objectMapper;
  private ResourceLoader resourceLoader;
  private DocumentSchemaRegistry documentSchemaRegistry;

  @BeforeEach
  @SneakyThrows
  void setup() {
    resourceLoader = new DefaultResourceLoader();
    objectMapper = new ObjectMapper().enable(ORDER_MAP_ENTRIES_BY_KEYS);
    documentSchemaRegistry = new DocumentSchemaRegistryImpl(objectMapper)
        .register(
            "1",
            Set.of("open"),
            objectMapper.readValue(load("classpath:__fixtures__/ord-doc-full.json"), DocumentSchema.class));
  }

  @Test
  @DisplayName("Runtime public only → snapshot (pretty-printed)")
  void runtimePublicSnapshot() throws Exception {
    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-cli.json"),
        objectMapper.writeValueAsString(documentSchemaRegistry
            .lookupDocumentSchema("1", Set.of("public", "internal"))
            .orElseThrow()),
        false);
  }

  @SneakyThrows
  private String pretty(DocumentSchema document) {
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
  }

  @SneakyThrows
  private String load(String path) {
    return resourceLoader.getResource(path).getContentAsString(UTF_8);
  }
}
