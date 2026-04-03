package org.openresourcediscovery.core.services;

import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.model.DocumentSchema;

public interface DocumentSchemaDetector {

  Map<String, Pair<DocumentSchema, Set<String>>> detect(OrdProperties properties);
}
