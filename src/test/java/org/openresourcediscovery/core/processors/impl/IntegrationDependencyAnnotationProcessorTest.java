package org.openresourcediscovery.core.processors.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner.ScanResult;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.IntegrationDependency;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class IntegrationDependencyAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.IntegrationDependency, IntegrationDependency> entityGenerator;

  @Mock
  private Ord.IntegrationDependency integrationDependencyAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.IntegrationDependency> customizer;

  private IntegrationDependencyAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new IntegrationDependencyAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenIntegrationDependenciesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    IntegrationDependency generatedDependency = new IntegrationDependency().withOrdId("integration-dep-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(integrationDependencyAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), integrationDependencyAnnotation)));
    when(entityGenerator.generate(
            EntityGenerator.Context.of(integrationDependencyAnnotation, getClass(), document)))
        .thenReturn(generatedDependency);

    classUnderTest.process(documents);

    assertThat(document.getIntegrationDependencies()).containsExactly(generatedDependency);

    verify(customizer).customize(integrationDependencyAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenDocumentAlreadyHasIntegrationDependencies_whenProcessIsCalled_thenNewDependencyIsAppended() {
    IntegrationDependency existingDependency = new IntegrationDependency().withOrdId("existing-dep");
    IntegrationDependency newDependency = new IntegrationDependency().withOrdId("new-dep");
    DocumentSchema document = new DocumentSchema();
    document.setIntegrationDependencies(List.of(existingDependency));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(integrationDependencyAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), integrationDependencyAnnotation)));
    when(entityGenerator.generate(
            EntityGenerator.Context.of(integrationDependencyAnnotation, getClass(), document)))
        .thenReturn(newDependency);

    classUnderTest.process(documents);

    assertThat(document.getIntegrationDependencies()).containsExactly(existingDependency, newDependency);

    verify(customizer).customize(integrationDependencyAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentIntegrationDependenciesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getIntegrationDependencies()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.IntegrationDependency.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.IntegrationDependency.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForIntegrationDependencyAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.IntegrationDependency.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllIntegrationDependenciesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.IntegrationDependency secondAnnotation = mock(Ord.IntegrationDependency.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    IntegrationDependency firstDependency = new IntegrationDependency().withOrdId("integration-dep-1");
    IntegrationDependency secondDependency = new IntegrationDependency().withOrdId("integration-dep-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.IntegrationDependency, IntegrationDependency>create(
            Ord.IntegrationDependency.class))
        .thenReturn(entityGenerator);
    when(integrationDependencyAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.IntegrationDependency.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), integrationDependencyAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(
            EntityGenerator.Context.of(integrationDependencyAnnotation, getClass(), document)))
        .thenReturn(firstDependency);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondDependency);

    classUnderTest.process(documents);

    assertThat(document.getIntegrationDependencies()).containsExactly(firstDependency, secondDependency);

    verify(customizer).customize(integrationDependencyAnnotation, document);
    verify(customizer).customize(secondAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }
}
