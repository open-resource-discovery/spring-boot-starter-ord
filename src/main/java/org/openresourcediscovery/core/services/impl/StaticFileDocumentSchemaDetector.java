package org.openresourcediscovery.core.services.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.core.io.ResourceLoader;

public class StaticFileDocumentSchemaDetector implements DocumentSchemaDetector {

  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;

  public StaticFileDocumentSchemaDetector(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Map<String, Pair<DocumentSchema, Set<String>>> detect(OrdProperties properties) {
    return properties.getDocuments().stream()
        .collect(toMap(
            OrdProperties.Document::getId,
            d -> ImmutablePair.of(loadDocumentSchema(d.getPath()), Set.copyOf(d.getAccessStrategies()))));
  }

  @SneakyThrows
  private DocumentSchema loadDocumentSchema(String path) {
    return objectMapper.readValue(resourceLoader.getResource(path).getContentAsString(UTF_8), DocumentSchema.class);
  }
}
