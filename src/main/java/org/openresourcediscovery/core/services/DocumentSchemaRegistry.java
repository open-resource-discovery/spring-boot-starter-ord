package org.openresourcediscovery.core.services;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.openresourcediscovery.model.DocumentSchema;

public interface DocumentSchemaRegistry {

  Map<String, DocumentSchema> getAllDocumentSchemas();

  Set<String> lookupAccessStrategies(String id);

  Optional<DocumentSchema> lookupDocumentSchema(String id, Set<String> visibilities);
}
