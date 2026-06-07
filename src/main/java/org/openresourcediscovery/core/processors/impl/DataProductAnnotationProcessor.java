package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class DataProductAnnotationProcessor implements AnnotationProcessor<Ord.DataProduct, DataProduct> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.DataProduct>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.DataProduct, DataProduct> entityGenerator =
        entityGeneratorFactory.create(Ord.DataProduct.class);

    ordAnnotationsScanner.scan(Ord.DataProduct.class).forEach(dp -> {
      DocumentSchema document =
          documents.get(dp.annotation().partOfDocument().name()).document();

      document.setDataProducts(ListUtils.union(
          emptyIfNull(document.getDataProducts()),
          List.of(entityGenerator.generate(Context.of(dp.annotation(), dp.annotated(), document)))));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(dp.annotation(), document));
    });
  }
}
