package org.openresourcediscovery.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class OrdDocumentBuildVsRuntimeTest {

  private ObjectMapper objectMapper;
  private Authentication authentication;
  private ResourceLoader resourceLoader;
  private SecurityContext securityContext;
  private DocumentSchemaRegistry documentSchemaRegistry;
  private StaticResourceRegistry staticResourceRegistry;
  private OrdAuthenticationManager ordAuthenticationManager;

  @BeforeEach
  @SneakyThrows
  void setUp() {
    objectMapper = new ObjectMapper();
    authentication = mock(Authentication.class);
    resourceLoader = new DefaultResourceLoader();
    securityContext = mock(SecurityContext.class);
    staticResourceRegistry = new StaticResourceRegistryImpl();
    ordAuthenticationManager = mock(OrdAuthenticationManager.class);
    documentSchemaRegistry = new DocumentSchemaRegistryImpl(objectMapper)
        .register(
            "1",
            Set.of("open"),
            objectMapper.readValue(load("classpath:__fixtures__/ord-doc-full.json"), DocumentSchema.class));

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("CLI build -> full document (internal + private)")
  void cli_full_snapshot() throws Exception {
    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-cli.json"),
        objectMapper.writeValueAsString(documentSchemaRegistry
            .lookupDocumentSchema("1", Set.of("public", "internal"))
            .orElseThrow()),
        false);
  }

  @Test
  @DisplayName("Runtime open -> public only")
  void runtime_open_snapshot() throws Exception {
    var result = standaloneSetup(new OpenResourceDiscoveryController(
            documentSchemaRegistry, staticResourceRegistry, Optional.of(ordAuthenticationManager)))
        .build()
        .perform(get("/ord/v1/documents/1").accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-runtime-open.json"),
        result.getResponse().getContentAsString(),
        false);
  }

  @Test
  @DisplayName("Runtime basic -> internal + public")
  void runtime_basic_snapshot() throws Exception {
    when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(ordAuthenticationManager.isAuthenticated(any())).thenReturn(Boolean.TRUE);

    var result = standaloneSetup(new OpenResourceDiscoveryController(
            documentSchemaRegistry, staticResourceRegistry, Optional.of(ordAuthenticationManager)))
        .build()
        .perform(get("/ord/v1/documents/1")
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, basic("user", "pass")))
        .andExpect(status().isOk())
        .andReturn();

    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-runtime-basic.json"),
        result.getResponse().getContentAsString(),
        false);
  }

  @SneakyThrows
  private String load(String path) {
    return resourceLoader.getResource(path).getContentAsString(UTF_8);
  }

  private static String basic(String user, String pass) {
    return "Basic %s"
        .formatted(Base64.getEncoder()
            .encodeToString("%s:%s".formatted(user, pass).getBytes(UTF_8)));
  }
}
