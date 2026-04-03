package org.openresourcediscovery.api.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Base64;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openresourcediscovery.core.configurations.OrdAutoConfiguration;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.impl.DocumentSchemaRegistryImpl;
import org.openresourcediscovery.model.DocumentSchema;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableWebSecurity
@AutoConfigureMockMvc
@SpringBootTest(
    classes = {
      BasicAuthOrdControllerSnapshotTest.SpringBootTestConfig.class,
      OrdAutoConfiguration.class,
    },
    properties = {
      "ord.autoconfigure=true",
      "ord.documents.0.id=1",
      "ord.documents.0.accessStrategies.0=basic-auth",
      "ord.documents.0.path=classpath:__fixtures__/ord-doc-full.json",
      "ord.credentials.admin={bcrypt}$2a$12$te68x8ajPZgD/icO90c0N.N23L0Igd8FN9n0XAv/Al1HFJVAMKoB2",
    })
class BasicAuthOrdControllerSnapshotTest {

  public static class SpringBootTestConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
      return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Bean
    @Primary
    @SneakyThrows
    public DocumentSchemaRegistry documentRegistry(OrdProperties properties) {
      return spy(properties.getDocuments().stream()
          .reduce(
              (DocumentSchemaRegistry) new DocumentSchemaRegistryImpl(objectMapper()),
              (r, d) -> r.register(d.getId(), d.getAccessStrategies(), load(d.getPath(), objectMapper())),
              (r1, r2) -> r1));
    }

    @SneakyThrows
    private DocumentSchema load(String path, ObjectMapper mapper) {
      return mapper.readValue(resourceLoader.getResource(path).getContentAsString(UTF_8), DocumentSchema.class);
    }
  }

  private static final String USERNAME = "admin";
  private static final String PASSWORD = "admin";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private DocumentSchemaRegistry documentSchemaRegistry;

  @AfterEach
  public void tearDown() {
    Mockito.reset(documentSchemaRegistry);
  }

  @Test
  @DisplayName("basic mode + valid auth → internal snapshot (real data)")
  void basic_valid_internal_snapshot() throws Exception {
    var result = mvc.perform(get("/ord/v1/documents/1")
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, basic(USERNAME, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();

    JSONAssert.assertEquals(
        load("classpath:__snapshots__/ord-document-basic.json"),
        pretty(result.getResponse().getContentAsString()),
        false);
  }

  @Test
  @DisplayName("basic mode + missing/invalid auth → 401")
  void basic_invalid_401() throws Exception {
    mvc.perform(get("/ord/v1/documents/1"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Basic")));

    verify(documentSchemaRegistry, never()).lookupDocumentSchema(any(), any());
  }

  @Test
  @DisplayName("service error → 500")
  void service_error_500() throws Exception {
    doThrow(new RuntimeException()).when(documentSchemaRegistry).lookupDocumentSchema(any(), any());

    mvc.perform(get("/ord/v1/documents/1")
            .accept(APPLICATION_JSON)
            .header(AUTHORIZATION, basic(USERNAME, PASSWORD)))
        .andExpect(status().isInternalServerError());
  }

  @Test
  @DisplayName("basic mode + valid auth -> well-known snapshot")
  void wellKnown_basic_snapshot() throws Exception {
    var result = mvc.perform(
            get("/.well-known/open-resource-discovery").header(AUTHORIZATION, basic(USERNAME, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn();

    JSONAssert.assertEquals(
        load("classpath:__snapshots__/well-known-basic.json"),
        pretty(result.getResponse().getContentAsString()),
        false);
  }

  @SneakyThrows
  private String pretty(String json) {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json));
  }

  private static String basic(String username, String password) {
    return "Basic %s"
        .formatted(Base64.getEncoder()
            .encodeToString("%s:%s".formatted(username, password).getBytes(UTF_8)));
  }

  @SneakyThrows
  private String load(String path) {
    return resourceLoader.getResource(path).getContentAsString(UTF_8);
  }
}
