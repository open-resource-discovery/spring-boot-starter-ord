package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.DocumentSchema;

@Setter(onMethod = @__({@Resource}))
public class ConsumptionBundleAnnotationProcessor
    implements AnnotationProcessor<Ord.ConsumptionBundle, ConsumptionBundle> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.ConsumptionBundle, ConsumptionBundle> entityGenerator =
        entityGeneratorFactory.create(Ord.ConsumptionBundle.class);

    ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class).forEach(cb -> {
      DocumentSchema document =
          documents.get(cb.getRight().partOfDocument().id()).getLeft();

      document.setConsumptionBundles(ListUtils.union(
          emptyIfNull(document.getConsumptionBundles()),
          List.of(entityGenerator.generate(Context.of(cb.getRight(), cb.getLeft(), document)))));
    });
  }
}
