package org.openresourcediscovery.core.services.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.databind.json.JsonMapper;

public class StaticFileDocumentSchemaDetector implements DocumentSchemaDetector {

  private final JsonMapper jsonMapper;
  private final ResourceLoader resourceLoader;

  public StaticFileDocumentSchemaDetector(JsonMapper jsonMapper, ResourceLoader resourceLoader) {
    this.jsonMapper = jsonMapper;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public Map<String, DetectionResult> detect(OrdProperties properties) {
    return properties.getDocuments().stream()
        .filter(document -> isNotEmpty(document.getPath()))
        .collect(toMap(
            OrdProperties.Document::getName,
            d -> new DetectionResult(
                loadDocumentSchema(d.getPath()), Set.copyOf(d.getAccessStrategies()))));
  }

  @SneakyThrows
  private DocumentSchema loadDocumentSchema(String path) {
    return jsonMapper.readValue(resourceLoader.getResource(path).getContentAsString(UTF_8), DocumentSchema.class);
  }
}
