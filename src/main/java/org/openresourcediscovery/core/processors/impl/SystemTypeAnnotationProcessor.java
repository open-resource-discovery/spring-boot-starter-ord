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
import org.openresourcediscovery.model.SystemType;

@Setter(onMethod = @__({@Resource}))
public class SystemTypeAnnotationProcessor implements AnnotationProcessor<Ord.SystemType, SystemType> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.SystemType, SystemType> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemType.class);

    ordAnnotationsScanner.scan(Ord.SystemType.class).forEach(st -> {
      DocumentSchema document =
          documents.get(st.getRight().partOfDocument().id()).getLeft();

      if (nonNull(document.getDescribedSystemType())) {
        throw new IllegalStateException("SystemType already specified");
      }

      document.setDescribedSystemType(
          entityGenerator.generate(Context.of(st.getRight(), st.getLeft(), document)));
    });
  }
}
