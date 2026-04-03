package org.openresourcediscovery.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Package;

@ExtendWith(MockitoExtension.class)
class JavaAnnotationsDocumentSchemaDetectorTest {

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private AnnotationProcessorFactory annotationProcessorFactory;

  private JavaAnnotationsDocumentSchemaDetector classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new JavaAnnotationsDocumentSchemaDetector(
        ordAnnotationsScanner, entityGeneratorFactory, annotationProcessorFactory);

    stubProcessorFor(Ord.Group.class);
    stubProcessorFor(Ord.Agent.class);
    stubProcessorFor(Ord.Vendor.class);
    stubProcessorFor(Ord.Product.class);
    stubProcessorFor(Ord.Package.class);
    stubProcessorFor(Ord.GroupType.class);
    stubProcessorFor(Ord.Tombstone.class);
    stubProcessorFor(Ord.EntityType.class);
    stubProcessorFor(Ord.Capability.class);
    stubProcessorFor(Ord.SystemType.class);
    stubProcessorFor(Ord.DataProduct.class);
    stubProcessorFor(Ord.ApiResource.class);
    stubProcessorFor(Ord.EventResource.class);
    stubProcessorFor(Ord.SystemVersion.class);
    stubProcessorFor(Ord.SystemInstance.class);
    stubProcessorFor(Ord.ConsumptionBundle.class);
    stubProcessorFor(Ord.IntegrationDependency.class);
  }

  @Test
  void givenNoDocuments_whenDetectIsCalled_thenEmptyMapIsReturned() {
    doReturn(List.of()).when(ordAnnotationsScanner).scan(eq(Ord.Document.class));
    doReturn(mock(EntityGenerator.class)).when(entityGeneratorFactory).create(Ord.Document.class);

    Map<String, Pair<DocumentSchema, Set<String>>> result =
        classUnderTest.detect(OrdProperties.builder().build());

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void givenOneDocument_whenDetectIsCalled_thenDocumentIsIndexedById() {
    DocumentSchema schema = new DocumentSchema();
    Ord.Document docAnnotation = mock(Ord.Document.class);
    EntityGenerator mockGenerator = mock(EntityGenerator.class);

    doReturn(schema).when(mockGenerator).generate(any());
    doReturn("my-doc-id").when(docAnnotation).id();
    doReturn(new Ord.AccessStrategy[0]).when(docAnnotation).accessStrategies();
    doReturn(mockGenerator).when(entityGeneratorFactory).create(Ord.Document.class);
    doReturn(List.of(ImmutablePair.of(String.class, docAnnotation)))
        .when(ordAnnotationsScanner)
        .scan(eq(Ord.Document.class));

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(
        OrdProperties.builder().packages(List.of("com.example")).build());

    assertEquals(1, result.size());
    assertTrue(result.containsKey("my-doc-id"));
    assertEquals(schema, result.get("my-doc-id").getLeft());
    assertTrue(result.get("my-doc-id").getRight().isEmpty());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void givenDocumentWithAccessStrategies_whenDetectIsCalled_thenStrategiesAreCollected() {
    Ord.Document docAnnotation = mock(Ord.Document.class);
    EntityGenerator mockGenerator = mock(EntityGenerator.class);
    Ord.AccessStrategy strategy = mock(Ord.AccessStrategy.class);

    doReturn("open").when(strategy).type();
    doReturn(new DocumentSchema()).when(mockGenerator).generate(any());
    doReturn("doc-with-strategies").when(docAnnotation).id();
    doReturn(mockGenerator).when(entityGeneratorFactory).create(Ord.Document.class);
    doReturn(new Ord.AccessStrategy[] {strategy}).when(docAnnotation).accessStrategies();
    doReturn(List.of(ImmutablePair.of(String.class, docAnnotation)))
        .when(ordAnnotationsScanner)
        .scan(eq(Ord.Document.class));

    Map<String, Pair<DocumentSchema, Set<String>>> result =
        classUnderTest.detect(OrdProperties.builder().build());

    assertEquals(Set.of("open"), result.get("doc-with-strategies").getRight());
  }

  @Test
  void whenDetectIsCalled_thenAllAnnotationProcessorsAreInvoked() {
    doReturn(List.of()).when(ordAnnotationsScanner).scan(eq(Ord.Document.class));
    doReturn(mock(EntityGenerator.class)).when(entityGeneratorFactory).create(Ord.Document.class);

    classUnderTest.detect(OrdProperties.builder().build());

    verify(annotationProcessorFactory.create(Ord.Vendor.class)).process(any());
    verify(annotationProcessorFactory.create(Ord.ApiResource.class)).process(any());
  }

  @Test
  void givenPackages_whenDetectIsCalled_thenScannerIsCalledWithPackages() {
    doReturn(List.of()).when(ordAnnotationsScanner).scan(eq(Ord.Document.class));
    doReturn(mock(EntityGenerator.class)).when(entityGeneratorFactory).create(Ord.Document.class);

    classUnderTest.detect(OrdProperties.builder()
        .packages(List.of("com.example", "org.test"))
        .build());

    verify(ordAnnotationsScanner).scan(Ord.Document.class);
  }

  @Test
  void givenNoDocuments_whenLookupDocumentsIsCalled_thenProcessorsAreNotInvoked() {
    doReturn(List.of()).when(ordAnnotationsScanner).scan(eq(Ord.Document.class));
    doReturn(mock(EntityGenerator.class)).when(entityGeneratorFactory).create(Ord.Document.class);

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.lookupDocuments();

    assertTrue(result.isEmpty());
    verify(annotationProcessorFactory, never()).create(any());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void givenDocumentWithNoPackages_whenDetectIsCalled_thenDefaultPackageIsAdded() {
    Ord.Document docAnnotation = mock(Ord.Document.class);
    EntityGenerator mockGenerator = mock(EntityGenerator.class);

    doReturn("doc-id").when(docAnnotation).id();
    doReturn(new DocumentSchema()).when(mockGenerator).generate(any());
    doReturn(new Ord.AccessStrategy[0]).when(docAnnotation).accessStrategies();
    doReturn(mockGenerator).when(entityGeneratorFactory).create(Ord.Document.class);
    doReturn("customer.test.namespace").when(ordProperties).getNamespace();
    doReturn(List.of(ImmutablePair.of(String.class, docAnnotation)))
        .when(ordAnnotationsScanner)
        .scan(eq(Ord.Document.class));

    Map<String, Pair<DocumentSchema, Set<String>>> result = classUnderTest.detect(ordProperties);

    assertEquals(
        List.of(new Package()
            .withVersion("1.0.0")
            .withTitle("Default Package")
            .withVendor("customer:vendor:Customer:")
            .withDescription("Auto-generated description for Default Package")
            .withOrdId("%s:package:default:v1".formatted(ordProperties.getNamespace()))
            .withShortDescription("Auto-generated short description for Default Package")),
        result.get("doc-id").getLeft().getPackages());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void givenDocumentWithExistingPackages_whenDetectIsCalled_thenDefaultPackageIsNotAdded() {
    Ord.Document docAnnotation = mock(Ord.Document.class);
    EntityGenerator mockGenerator = mock(EntityGenerator.class);
    Package existingPackage = new Package().withOrdId("customer.test.namespace:package:existing:v1");
    DocumentSchema schema = new DocumentSchema().withPackages(List.of(existingPackage));

    doReturn(schema).when(mockGenerator).generate(any());
    doReturn("doc-id").when(docAnnotation).id();
    doReturn(new Ord.AccessStrategy[0]).when(docAnnotation).accessStrategies();
    doReturn(mockGenerator).when(entityGeneratorFactory).create(Ord.Document.class);
    doReturn(List.of(ImmutablePair.of(String.class, docAnnotation)))
        .when(ordAnnotationsScanner)
        .scan(eq(Ord.Document.class));

    Map<String, Pair<DocumentSchema, Set<String>>> result =
        classUnderTest.detect(OrdProperties.builder().build());

    assertEquals(List.of(existingPackage), result.get("doc-id").getLeft().getPackages());
  }

  private <A extends java.lang.annotation.Annotation> void stubProcessorFor(Class<A> annotationClass) {
    AnnotationProcessor<?, ?> processor = mock(AnnotationProcessor.class);
    lenient().doReturn(processor).when(annotationProcessorFactory).create(annotationClass);
  }
}
