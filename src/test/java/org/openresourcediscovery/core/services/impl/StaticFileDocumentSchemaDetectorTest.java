package org.openresourcediscovery.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class StaticFileDocumentSchemaDetectorTest {

  private static final String DOC_ID = "doc-1";
  private static final String DOC_PATH = "classpath:__fixtures__/ord-doc-full.json";
  private static final String ACCESS_STRATEGY = "open";

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private Resource resource;

  private StaticFileDocumentSchemaDetector classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new StaticFileDocumentSchemaDetector(objectMapper, resourceLoader);
  }

  @Test
  void givenEmptyDocuments_whenDetectIsCalled_thenEmptyMapIsReturned() {
    OrdProperties properties = OrdProperties.builder().documents(List.of()).build();

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(properties);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void givenSingleDocument_whenDetectIsCalled_thenDocumentIsLoaded() throws IOException {
    OrdProperties.Document document = OrdProperties.Document.builder()
        .id(DOC_ID)
        .path(DOC_PATH)
        .accessStrategies(Set.of(ACCESS_STRATEGY))
        .build();
    OrdProperties properties =
        OrdProperties.builder().documents(List.of(document)).build();
    DocumentSchema expectedSchema = new DocumentSchema();
    doReturn(resource).when(resourceLoader).getResource(DOC_PATH);
    doReturn("{}").when(resource).getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    doReturn(expectedSchema).when(objectMapper).readValue("{}", DocumentSchema.class);

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(properties);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey(DOC_ID));
    assertEquals(expectedSchema, result.get(DOC_ID).getLeft());
    assertEquals(Set.of(ACCESS_STRATEGY), result.get(DOC_ID).getRight());
  }

  @Test
  void givenDocumentWithNoAccessStrategies_whenDetectIsCalled_thenEmptySetIsReturned() throws IOException {
    OrdProperties.Document document =
        OrdProperties.Document.builder().id(DOC_ID).path(DOC_PATH).build();
    OrdProperties properties =
        OrdProperties.builder().documents(List.of(document)).build();
    DocumentSchema expectedSchema = new DocumentSchema();
    doReturn(resource).when(resourceLoader).getResource(DOC_PATH);
    doReturn("{}").when(resource).getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    doReturn(expectedSchema).when(objectMapper).readValue("{}", DocumentSchema.class);

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(properties);

    assertTrue(result.get(DOC_ID).getRight().isEmpty());
  }

  @Test
  void givenMultipleDocuments_whenDetectIsCalled_thenAllDocumentsAreLoaded() throws IOException {
    OrdProperties.Document doc1 = OrdProperties.Document.builder()
        .id("doc-1")
        .path("classpath:doc1.json")
        .build();
    OrdProperties.Document doc2 = OrdProperties.Document.builder()
        .id("doc-2")
        .path("classpath:doc2.json")
        .build();
    OrdProperties properties =
        OrdProperties.builder().documents(List.of(doc1, doc2)).build();
    Resource resource2 = org.mockito.Mockito.mock(Resource.class);
    doReturn(resource).when(resourceLoader).getResource("classpath:doc1.json");
    doReturn(resource2).when(resourceLoader).getResource("classpath:doc2.json");
    doReturn("{}").when(resource).getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    doReturn("{}").when(resource2).getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    doReturn(new DocumentSchema()).when(objectMapper).readValue("{}", DocumentSchema.class);

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(properties);

    assertEquals(2, result.size());
    assertTrue(result.containsKey("doc-1"));
    assertTrue(result.containsKey("doc-2"));
  }

  @Test
  void givenResourceLoadFails_whenDetectIsCalled_thenExceptionIsPropagated() throws IOException {
    OrdProperties.Document document =
        OrdProperties.Document.builder().id(DOC_ID).path(DOC_PATH).build();
    OrdProperties properties =
        OrdProperties.builder().documents(List.of(document)).build();
    doReturn(resource).when(resourceLoader).getResource(DOC_PATH);
    doThrow(new IOException("File not found"))
        .when(resource)
        .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

    assertThrows(Exception.class, () -> classUnderTest.detect(properties));
  }
}
