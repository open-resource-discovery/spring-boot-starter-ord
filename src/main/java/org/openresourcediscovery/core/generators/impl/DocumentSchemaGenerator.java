package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentSchema.OpenResourceDiscovery;

@Setter(onMethod = @__({@Resource}))
public class DocumentSchemaGenerator extends EntityAutoGenerator<Ord.Document, DocumentSchema> {

  private static final String DOCUMENT_SCHEMA_URL =
      "https://open-resource-discovery.github.io/specification/spec-v1/interfaces/Document.schema.json";

  public DocumentSchemaGenerator() {
    super(DocumentSchema::new);
  }

  @Override
  public DocumentSchema generate(Context<Ord.Document> context) {
    return super.generate(context)
        .with$schema(DOCUMENT_SCHEMA_URL)
        .withOpenResourceDiscovery(OpenResourceDiscovery._1_16);
  }
}
