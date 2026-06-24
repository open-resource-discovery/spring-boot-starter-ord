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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Product;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class ProductAnnotationProcessor implements AnnotationProcessor<Ord.Product, Product> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.Product>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Product, Product> entityGenerator = entityGeneratorFactory.create(Ord.Product.class);

    ordAnnotationsScanner.scan(Ord.Product.class).forEach(p -> {
      DocumentSchema document =
          documents.get(p.annotation().partOfDocument().name()).document();

      document.setProducts(ListUtils.union(
          emptyIfNull(document.getProducts()),
          List.of(entityGenerator.generate(Context.of(p.annotation(), p.annotated(), document)))));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(p.annotation(), document));
    });
  }
}
