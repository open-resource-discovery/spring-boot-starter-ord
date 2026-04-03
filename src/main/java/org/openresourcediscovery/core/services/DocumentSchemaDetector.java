package org.openresourcediscovery.core.services;

import java.util.Map;
import java.util.Set;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.model.DocumentSchema;

public interface DocumentSchemaDetector {

  record DetectionResult(DocumentSchema document, Set<String> strategies) {}

  Map<String, DetectionResult> detect(OrdProperties properties);
}
