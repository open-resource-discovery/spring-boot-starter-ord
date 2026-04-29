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
import org.openresourcediscovery.model.Capability;
import org.openresourcediscovery.model.DocumentSchema;

@Setter(onMethod = @__({@Resource}))
public class CapabilityAnnotationProcessor implements AnnotationProcessor<Ord.Capability, Capability> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Capability, Capability> entityGenerator =
        entityGeneratorFactory.create(Ord.Capability.class);

    ordAnnotationsScanner.scan(Ord.Capability.class).forEach(c -> {
      DocumentSchema document =
          documents.get(c.annotation().partOfDocument().name()).document();

      document.setCapabilities(ListUtils.union(
          emptyIfNull(document.getCapabilities()),
          List.of(entityGenerator.generate(Context.of(c.annotation(), c.annotated(), document)))));
    });
  }
}
