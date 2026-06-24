package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class ConsumptionBundleAnnotationProcessor
    implements AnnotationProcessor<Ord.ConsumptionBundle, ConsumptionBundle> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.ConsumptionBundle>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.ConsumptionBundle, ConsumptionBundle> entityGenerator =
        entityGeneratorFactory.create(Ord.ConsumptionBundle.class);

    ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class).forEach(cb -> {
      DocumentSchema document =
          documents.get(cb.annotation().partOfDocument().name()).document();

      document.setConsumptionBundles(ListUtils.union(
          emptyIfNull(document.getConsumptionBundles()),
          List.of(entityGenerator.generate(Context.of(cb.annotation(), cb.annotated(), document)))));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(cb.annotation(), document));
    });
  }
}
