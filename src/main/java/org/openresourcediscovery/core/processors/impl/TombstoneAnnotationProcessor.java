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
import org.openresourcediscovery.model.Tombstone;

@Setter(onMethod = @__({@Resource}))
public class TombstoneAnnotationProcessor implements AnnotationProcessor<Ord.Tombstone, Tombstone> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.Tombstone, Tombstone> entityGenerator = entityGeneratorFactory.create(Ord.Tombstone.class);

    ordAnnotationsScanner.scan(Ord.Tombstone.class).forEach(t -> {
      DocumentSchema document =
          documents.get(t.getRight().partOfDocument().id()).getLeft();

      document.setTombstones(ListUtils.union(
          emptyIfNull(document.getTombstones()),
          List.of(entityGenerator.generate(Context.of(t.getRight(), t.getLeft(), document)))));
    });
  }
}
