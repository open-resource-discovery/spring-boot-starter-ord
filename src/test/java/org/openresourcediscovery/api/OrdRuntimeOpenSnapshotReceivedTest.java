package org.openresourcediscovery.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.api.controllers.OpenResourceDiscoveryController;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.openresourcediscovery.core.services.impl.DocumentSchemaRegistryImpl;
import org.openresourcediscovery.core.services.impl.StaticResourceRegistryImpl;
import org.openresourcediscovery.model.DocumentSchema;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.databind.json.JsonMapper;

class OrdRuntimeOpenSnapshotReceivedTest {

  private JsonMapper jsonMapper;
  private ResourceLoader resourceLoader;
  private DocumentSchemaRegistry documentSchemaRegistry;
  private StaticResourceRegistry staticResourceRegistry;
  private OrdAuthenticationManager ordAuthenticationManager;

  @BeforeEach
  @SneakyThrows
  void setup() {
    resourceLoader = new DefaultResourceLoader();
    staticResourceRegistry = new StaticResourceRegistryImpl();
    ordAuthenticationManager = mock(OrdAuthenticationManager.class);
    jsonMapper = JsonMapper.builder().enable(ORDER_MAP_ENTRIES_BY_KEYS).build();
    documentSchemaRegistry = new DocumentSchemaRegistryImpl(jsonMapper)
        .register(
            "ord-document",
            Set.of("open"),
            jsonMapper.readValue(load("classpath:__fixtures__/ord-doc-full.json"), DocumentSchema.class));
  }

  @Test
  @DisplayName("Runtime open -> compare to snapshot")
  void runtime_open_snapshot_with_received_on_mismatch() throws Exception {
    var result = standaloneSetup(new OpenResourceDiscoveryController(
            documentSchemaRegistry, staticResourceRegistry, Optional.of(ordAuthenticationManager)))
        .build()
        .perform(get("/ord/v1/documents/ord-document").accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-runtime-open.json"),
        result.getResponse().getContentAsString(),
        false);
  }

  @SneakyThrows
  private String load(String path) {
    return resourceLoader.getResource(path).getContentAsString(UTF_8);
  }
}
