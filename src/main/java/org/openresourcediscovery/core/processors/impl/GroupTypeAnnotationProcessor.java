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
import org.openresourcediscovery.model.GroupType;

@Setter(onMethod = @__({@Resource}))
public class GroupTypeAnnotationProcessor implements AnnotationProcessor<Ord.GroupType, GroupType> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.GroupType, GroupType> entityGenerator = entityGeneratorFactory.create(Ord.GroupType.class);

    ordAnnotationsScanner.scan(Ord.GroupType.class).forEach(gt -> {
      DocumentSchema document =
          documents.get(gt.annotation().partOfDocument().name()).document();

      document.setGroupTypes(ListUtils.union(
          emptyIfNull(document.getGroupTypes()),
          List.of(entityGenerator.generate(Context.of(gt.annotation(), gt.annotated(), document)))));
    });
  }
}
