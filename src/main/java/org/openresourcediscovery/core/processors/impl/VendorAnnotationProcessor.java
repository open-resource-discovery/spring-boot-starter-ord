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
import org.openresourcediscovery.model.Vendor;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class VendorAnnotationProcessor implements AnnotationProcessor<Ord.Vendor, Vendor> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.Vendor>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Vendor, Vendor> entityGenerator = entityGeneratorFactory.create(Ord.Vendor.class);

    ordAnnotationsScanner.scan(Ord.Vendor.class).forEach(v -> {
      DocumentSchema document =
          documents.get(v.annotation().partOfDocument().name()).document();

      document.setVendors(ListUtils.union(
          emptyIfNull(document.getVendors()),
          List.of(entityGenerator.generate(Context.of(v.annotation(), v.annotated(), document)))));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(v.annotation(), document));
    });
  }
}
