package org.openresourcediscovery.core.services.impl;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Package;

@RequiredArgsConstructor
public class JavaAnnotationsDocumentSchemaDetector implements DocumentSchemaDetector {

  private final OrdAnnotationsScanner ordAnnotationsScanner;
  private final EntityGeneratorFactory entityGeneratorFactory;
  private final AnnotationProcessorFactory annotationProcessorFactory;

  public Map<String, DetectionResult> detect(OrdProperties ordProperties) {
    Map<String, DetectionResult> documents = lookupDocuments();

    // Order is important
    annotationProcessorFactory.create(Ord.Vendor.class).process(documents);
    annotationProcessorFactory.create(Ord.SystemType.class).process(documents);
    annotationProcessorFactory.create(Ord.SystemVersion.class).process(documents);
    annotationProcessorFactory.create(Ord.SystemInstance.class).process(documents);
    annotationProcessorFactory.create(Ord.ConsumptionBundle.class).process(documents);
    annotationProcessorFactory.create(Ord.Product.class).process(documents);
    annotationProcessorFactory.create(Ord.Package.class).process(documents);
    annotationProcessorFactory.create(Ord.GroupType.class).process(documents);
    annotationProcessorFactory.create(Ord.Group.class).process(documents);
    annotationProcessorFactory.create(Ord.Tombstone.class).process(documents);
    annotationProcessorFactory.create(Ord.Agent.class).process(documents);
    annotationProcessorFactory.create(Ord.EntityType.class).process(documents);
    annotationProcessorFactory.create(Ord.Capability.class).process(documents);
    annotationProcessorFactory.create(Ord.DataProduct.class).process(documents);
    annotationProcessorFactory.create(Ord.ApiResource.class).process(documents);
    annotationProcessorFactory.create(Ord.EventResource.class).process(documents);
    annotationProcessorFactory.create(Ord.IntegrationDependency.class).process(documents);

    documents.values().stream()
        .map(DetectionResult::document)
        .filter(document -> isEmpty(document.getPackages()))
        .forEach(document -> document.setPackages(List.of(createDefaultPackage(ordProperties))));

    return documents;
  }

  protected Package createDefaultPackage(OrdProperties ordProperties) {
    return new Package()
        .withVersion("1.0.0")
        .withTitle("Default Package")
        .withVendor("customer:vendor:Customer:")
        .withDescription("Auto-generated description for Default Package")
        .withOrdId("%s:package:default:v1".formatted(ordProperties.getNamespace()))
        .withShortDescription("Auto-generated short description for Default Package");
  }

  protected Map<String, DetectionResult> lookupDocuments() {
    Map<String, DetectionResult> documents = new HashMap<>();
    EntityGenerator<Ord.Document, DocumentSchema> generator = entityGeneratorFactory.create(Ord.Document.class);

    ordAnnotationsScanner
        .scan(Ord.Document.class)
        .forEach(d -> documents.put(
            d.annotation().name(),
            new DetectionResult(
                generator.generate(Context.of(d.annotation(), d.annotated(), null)),
                Stream.of(d.annotation().accessStrategies())
                    .map(Ord.AccessStrategy::type)
                    .collect(toSet()))));

    return documents;
  }
}
