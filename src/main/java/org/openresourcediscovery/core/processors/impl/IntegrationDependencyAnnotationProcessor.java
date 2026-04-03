package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.IntegrationDependency;

@Setter(onMethod = @__({@Resource}))
public class IntegrationDependencyAnnotationProcessor
    implements AnnotationProcessor<Ord.IntegrationDependency, IntegrationDependency> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.IntegrationDependency, IntegrationDependency> entityGenerator =
        entityGeneratorFactory.create(Ord.IntegrationDependency.class);

    ordAnnotationsScanner.scan(Ord.IntegrationDependency.class).forEach(id -> {
      DocumentSchema document =
          documents.get(id.annotation().partOfDocument().id()).document();

      document.setIntegrationDependencies(ListUtils.union(
          emptyIfNull(document.getIntegrationDependencies()),
          List.of(entityGenerator.generate(Context.of(id.annotation(), id.annotated(), document)))));
    });
  }
}
