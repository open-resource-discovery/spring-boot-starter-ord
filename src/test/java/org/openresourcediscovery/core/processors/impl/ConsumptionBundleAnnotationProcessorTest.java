package org.openresourcediscovery.core.processors.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner.ScanResult;
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.DocumentSchema;

@ExtendWith(MockitoExtension.class)
class ConsumptionBundleAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.ConsumptionBundle, ConsumptionBundle> entityGenerator;

  @Mock
  private Ord.ConsumptionBundle consumptionBundleAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private ConsumptionBundleAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ConsumptionBundleAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenConsumptionBundlesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    ConsumptionBundle generatedConsumptionBundle = new ConsumptionBundle().withOrdId("bundle-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(consumptionBundleAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), consumptionBundleAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(consumptionBundleAnnotation, getClass(), document)))
        .thenReturn(generatedConsumptionBundle);

    classUnderTest.process(documents);

    assertThat(document.getConsumptionBundles()).containsExactly(generatedConsumptionBundle);
  }

  @Test
  void givenDocumentAlreadyHasConsumptionBundles_whenProcessIsCalled_thenNewConsumptionBundleIsAppended() {
    ConsumptionBundle existingConsumptionBundle = new ConsumptionBundle().withOrdId("existing-bundle");
    ConsumptionBundle newConsumptionBundle = new ConsumptionBundle().withOrdId("new-bundle");
    DocumentSchema document = new DocumentSchema();
    document.setConsumptionBundles(List.of(existingConsumptionBundle));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(consumptionBundleAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), consumptionBundleAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(consumptionBundleAnnotation, getClass(), document)))
        .thenReturn(newConsumptionBundle);

    classUnderTest.process(documents);

    assertThat(document.getConsumptionBundles()).containsExactly(existingConsumptionBundle, newConsumptionBundle);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentConsumptionBundlesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getConsumptionBundles()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.ConsumptionBundle.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.ConsumptionBundle.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForConsumptionBundleAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.ConsumptionBundle.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllConsumptionBundlesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.ConsumptionBundle secondAnnotation = mock(Ord.ConsumptionBundle.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    ConsumptionBundle firstConsumptionBundle = new ConsumptionBundle().withOrdId("bundle-1");
    ConsumptionBundle secondConsumptionBundle = new ConsumptionBundle().withOrdId("bundle-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ConsumptionBundle, ConsumptionBundle>create(Ord.ConsumptionBundle.class))
        .thenReturn(entityGenerator);
    when(consumptionBundleAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.ConsumptionBundle.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), consumptionBundleAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(consumptionBundleAnnotation, getClass(), document)))
        .thenReturn(firstConsumptionBundle);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondConsumptionBundle);

    classUnderTest.process(documents);

    assertThat(document.getConsumptionBundles()).containsExactly(firstConsumptionBundle, secondConsumptionBundle);
  }
}
