package org.openresourcediscovery.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

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
import tools.jackson.databind.json.JsonMapper;

class OrdDocumentServiceSnapshotTest {

  private JsonMapper jsonMapper;
  private ResourceLoader resourceLoader;
  private DocumentSchemaRegistry documentSchemaRegistry;

  @BeforeEach
  @SneakyThrows
  void setup() {
    resourceLoader = new DefaultResourceLoader();
    jsonMapper = JsonMapper.builder().enable(ORDER_MAP_ENTRIES_BY_KEYS).build();
    documentSchemaRegistry = new DocumentSchemaRegistryImpl(jsonMapper)
        .register(
            "1",
            Set.of("open"),
            jsonMapper.readValue(load("classpath:__fixtures__/ord-doc-full.json"), DocumentSchema.class));
  }

  @Test
  @DisplayName("Runtime public only → snapshot (pretty-printed)")
  void runtimePublicSnapshot() throws Exception {
    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-cli.json"),
        jsonMapper.writeValueAsString(documentSchemaRegistry
            .lookupDocumentSchema("1", Set.of("public", "internal"))
            .orElseThrow()),
        false);
  }

  @SneakyThrows
  private String pretty(DocumentSchema document) {
    return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
  }

  @SneakyThrows
  private String load(String path) {
    return resourceLoader.getResource(path).getContentAsString(UTF_8);
  }
}
