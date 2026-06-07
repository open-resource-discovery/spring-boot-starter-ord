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
import org.openresourcediscovery.model.SystemVersion;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class SystemVersionAnnotationProcessor implements AnnotationProcessor<Ord.SystemVersion, SystemVersion> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.SystemVersion>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.SystemVersion, SystemVersion> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemVersion.class);

    ordAnnotationsScanner.scan(Ord.SystemVersion.class).forEach(sv -> {
      DocumentSchema document =
          documents.get(sv.annotation().partOfDocument().name()).document();

      if (nonNull(document.getDescribedSystemVersion())) {
        throw new IllegalStateException("SystemVersion already specified");
      }

      document.setDescribedSystemVersion(
          entityGenerator.generate(Context.of(sv.annotation(), sv.annotated(), document)));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(sv.annotation(), document));
    });
  }
}
