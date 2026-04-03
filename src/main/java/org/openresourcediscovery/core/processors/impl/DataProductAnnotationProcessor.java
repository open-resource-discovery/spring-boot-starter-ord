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
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DocumentSchema;

@Setter(onMethod = @__({@Resource}))
public class DataProductAnnotationProcessor implements AnnotationProcessor<Ord.DataProduct, DataProduct> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;

  @Override
  public void process(Map<String, Pair<DocumentSchema, Set<String>>> documents) {
    EntityGenerator<Ord.DataProduct, DataProduct> entityGenerator =
        entityGeneratorFactory.create(Ord.DataProduct.class);

    ordAnnotationsScanner.scan(Ord.DataProduct.class).forEach(dp -> {
      DocumentSchema document =
          documents.get(dp.getRight().partOfDocument().id()).getLeft();

      document.setDataProducts(ListUtils.union(
          emptyIfNull(document.getDataProducts()),
          List.of(entityGenerator.generate(Context.of(dp.getRight(), dp.getLeft(), document)))));
    });
  }
}
