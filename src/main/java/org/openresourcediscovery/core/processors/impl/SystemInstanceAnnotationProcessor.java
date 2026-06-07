package org.openresourcediscovery.core.processors.impl;

import static java.util.Objects.nonNull;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.SystemInstance;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class SystemInstanceAnnotationProcessor implements AnnotationProcessor<Ord.SystemInstance, SystemInstance> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.SystemInstance>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.SystemInstance, SystemInstance> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemInstance.class);

    ordAnnotationsScanner.scan(Ord.SystemInstance.class).forEach(si -> {
      DocumentSchema document =
          documents.get(si.annotation().partOfDocument().name()).document();

      if (nonNull(document.getDescribedSystemInstance())) {
        throw new IllegalStateException("SystemInstance already specified");
      }

      document.setDescribedSystemInstance(
          entityGenerator.generate(Context.of(si.annotation(), si.annotated(), document)));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(si.annotation(), document));
    });
  }
}
