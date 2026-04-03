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
import org.openresourcediscovery.model.EventResource;

@Setter(onMethod = @__({@Resource}))
public class EventResourceAnnotationProcessor implements AnnotationProcessor<Ord.EventResource, EventResource> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.EventResource, EventResource> entityGenerator =
        entityGeneratorFactory.create(Ord.EventResource.class);

    ordAnnotationsScanner.scan(Ord.EventResource.class).forEach(er -> {
      DocumentSchema document =
          documents.get(er.getRight().partOfDocument().id()).getLeft();

      document.setEventResources(ListUtils.union(
          emptyIfNull(document.getEventResources()),
          List.of(entityGenerator.generate(Context.of(er.getRight(), er.getLeft(), document)))));
    });
  }
}
