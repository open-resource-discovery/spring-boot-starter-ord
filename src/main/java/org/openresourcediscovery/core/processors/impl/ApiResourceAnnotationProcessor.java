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
import org.openresourcediscovery.model.ApiResource;
import org.openresourcediscovery.model.DocumentSchema;

@Setter(onMethod = @__({@Resource}))
public class ApiResourceAnnotationProcessor implements AnnotationProcessor<Ord.ApiResource, ApiResource> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.ApiResource, ApiResource> entityGenerator =
        entityGeneratorFactory.create(Ord.ApiResource.class);

    ordAnnotationsScanner.scan(Ord.ApiResource.class).forEach(ar -> {
      DocumentSchema document =
          documents.get(ar.annotation().partOfDocument().id()).document();

      document.setApiResources(ListUtils.union(
          emptyIfNull(document.getApiResources()),
          List.of(entityGenerator.generate(Context.of(ar.annotation(), ar.annotated(), document)))));
    });
  }
}
