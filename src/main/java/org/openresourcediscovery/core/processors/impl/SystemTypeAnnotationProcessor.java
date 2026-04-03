package org.openresourcediscovery.core.processors.impl;

import static java.util.Objects.nonNull;

import jakarta.annotation.Resource;
import java.util.Map;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.SystemType;

@Setter(onMethod = @__({@Resource}))
public class SystemTypeAnnotationProcessor implements AnnotationProcessor<Ord.SystemType, SystemType> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.SystemType, SystemType> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemType.class);

    ordAnnotationsScanner.scan(Ord.SystemType.class).forEach(st -> {
      DocumentSchema document =
          documents.get(st.annotation().partOfDocument().id()).document();

      if (nonNull(document.getDescribedSystemType())) {
        throw new IllegalStateException("SystemType already specified");
      }

      document.setDescribedSystemType(
          entityGenerator.generate(Context.of(st.annotation(), st.annotated(), document)));
    });
  }
}
