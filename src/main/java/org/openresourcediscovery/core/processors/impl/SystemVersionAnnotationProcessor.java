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
import org.openresourcediscovery.model.SystemVersion;

@Setter(onMethod = @__({@Resource}))
public class SystemVersionAnnotationProcessor implements AnnotationProcessor<Ord.SystemVersion, SystemVersion> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.SystemVersion, SystemVersion> entityGenerator =
        entityGeneratorFactory.create(Ord.SystemVersion.class);

    ordAnnotationsScanner.scan(Ord.SystemVersion.class).forEach(sv -> {
      DocumentSchema document =
          documents.get(sv.getRight().partOfDocument().id()).getLeft();

      if (nonNull(document.getDescribedSystemVersion())) {
        throw new IllegalStateException("SystemVersion already specified");
      }

      document.setDescribedSystemVersion(
          entityGenerator.generate(Context.of(sv.getRight(), sv.getLeft(), document)));
    });
  }
}
