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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EntityType;

@Setter(onMethod = @__({@Resource}))
public class EntityTypeAnnotationProcessor implements AnnotationProcessor<Ord.EntityType, EntityType> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.EntityType, EntityType> entityGenerator =
        entityGeneratorFactory.create(Ord.EntityType.class);

    ordAnnotationsScanner.scan(Ord.EntityType.class).forEach(et -> {
      DocumentSchema document =
          documents.get(et.getRight().partOfDocument().id()).getLeft();

      document.setEntityTypes(ListUtils.union(
          emptyIfNull(document.getEntityTypes()),
          List.of(entityGenerator.generate(Context.of(et.getRight(), et.getLeft(), document)))));
    });
  }
}
