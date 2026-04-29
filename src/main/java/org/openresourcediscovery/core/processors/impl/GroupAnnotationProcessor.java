package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Group;

@Setter(onMethod = @__({@Resource}))
public class GroupAnnotationProcessor implements AnnotationProcessor<Ord.Group, Group> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Group, Group> entityGenerator = entityGeneratorFactory.create(Ord.Group.class);

    ordAnnotationsScanner.scan(Ord.Group.class).forEach(g -> {
      DocumentSchema document =
          documents.get(g.annotation().partOfDocument().name()).document();

      document.setGroups(ListUtils.union(
          emptyIfNull(document.getGroups()),
          List.of(entityGenerator.generate(Context.of(g.annotation(), g.annotated(), document)))));
    });
  }
}
