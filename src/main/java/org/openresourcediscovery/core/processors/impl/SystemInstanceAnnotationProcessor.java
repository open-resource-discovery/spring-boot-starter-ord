package org.openresourcediscovery.core.processors.impl;

import static java.util.Objects.nonNull;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Set;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.SystemInstance;

@Setter(onMethod = @__({@Resource}))
public class SystemInstanceAnnotationProcessor implements AnnotationProcessor<Ord.SystemInstance, SystemInstance> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.SystemInstance, SystemInstance> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemInstance.class);

    ordAnnotationsScanner.scan(Ord.SystemInstance.class).forEach(si -> {
      DocumentSchema document =
          documents.get(si.getRight().partOfDocument().id()).getLeft();

      if (nonNull(document.getDescribedSystemInstance())) {
        throw new IllegalStateException("SystemInstance already specified");
      }

      document.setDescribedSystemInstance(
          entityGenerator.generate(Context.of(si.getRight(), si.getLeft(), document)));
    });
  }
}
