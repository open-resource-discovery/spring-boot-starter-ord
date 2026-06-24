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
import org.openresourcediscovery.model.Package;
import org.springframework.beans.factory.ObjectProvider;

@Setter(onMethod = @__({@Resource}))
public class PackageAnnotationProcessor implements AnnotationProcessor<Ord.Package, Package> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private ObjectProvider<Customizer<Ord.Package>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Package, Package> entityGenerator = entityGeneratorFactory.create(Ord.Package.class);

    ordAnnotationsScanner.scan(Ord.Package.class).forEach(p -> {
      DocumentSchema document =
          documents.get(p.annotation().partOfDocument().name()).document();

      document.setPackages(ListUtils.union(
          emptyIfNull(document.getPackages()),
          List.of(entityGenerator.generate(Context.of(p.annotation(), p.annotated(), document)))));

      Optional.ofNullable(customizers)
          .map(ObjectProvider::orderedStream)
          .orElse(Stream.empty())
          .forEach(customizer -> customizer.customize(p.annotation(), document));
    });
  }
}
