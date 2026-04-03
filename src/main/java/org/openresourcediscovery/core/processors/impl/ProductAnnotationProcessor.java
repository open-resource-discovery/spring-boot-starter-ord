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
import org.openresourcediscovery.model.Product;

@Setter(onMethod = @__({@Resource}))
public class ProductAnnotationProcessor implements AnnotationProcessor<Ord.Product, Product> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.Product, Product> entityGenerator = entityGeneratorFactory.create(Ord.Product.class);

    ordAnnotationsScanner.scan(Ord.Product.class).forEach(p -> {
      DocumentSchema document =
          documents.get(p.getRight().partOfDocument().id()).getLeft();

      document.setProducts(ListUtils.union(
          emptyIfNull(document.getProducts()),
          List.of(entityGenerator.generate(Context.of(p.getRight(), p.getLeft(), document)))));
    });
  }
}
