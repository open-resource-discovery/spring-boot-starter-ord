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
import org.openresourcediscovery.model.Overlay;

@Setter(onMethod = @__({@Resource}))
public class OverlayAnnotationProcessor implements AnnotationProcessor<Ord.Overlay, Overlay> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Overlay, Overlay> entityGenerator = entityGeneratorFactory.create(Ord.Overlay.class);

    ordAnnotationsScanner.scan(Ord.Overlay.class).forEach(a -> {
      DocumentSchema document =
          documents.get(a.annotation().partOfDocument().name()).document();

      document.setOverlays(ListUtils.union(
          emptyIfNull(document.getOverlays()),
          List.of(entityGenerator.generate(Context.of(a.annotation(), a.annotated(), document)))));
    });
  }
}
