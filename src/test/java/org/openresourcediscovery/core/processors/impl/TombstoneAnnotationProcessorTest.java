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
import org.openresourcediscovery.model.Tombstone;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class TombstoneAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Tombstone, Tombstone> entityGenerator;

  @Mock
  private Ord.Tombstone tombstoneAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.Tombstone> customizer;

  private TombstoneAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new TombstoneAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenTombstonesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Tombstone generatedTombstone = new Tombstone().withOrdId("tombstone-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(tombstoneAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), tombstoneAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(tombstoneAnnotation, getClass(), document)))
        .thenReturn(generatedTombstone);

    classUnderTest.process(documents);

    assertThat(document.getTombstones()).containsExactly(generatedTombstone);

    verify(customizer).customize(tombstoneAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenDocumentAlreadyHasTombstones_whenProcessIsCalled_thenNewTombstoneIsAppended() {
    Tombstone existingTombstone = new Tombstone().withOrdId("existing-tombstone");
    Tombstone newTombstone = new Tombstone().withOrdId("new-tombstone");
    DocumentSchema document = new DocumentSchema();
    document.setTombstones(List.of(existingTombstone));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(tombstoneAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), tombstoneAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(tombstoneAnnotation, getClass(), document)))
        .thenReturn(newTombstone);

    classUnderTest.process(documents);

    assertThat(document.getTombstones()).containsExactly(existingTombstone, newTombstone);

    verify(customizer).customize(tombstoneAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentTombstonesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getTombstones()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Tombstone.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Tombstone.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForTombstoneAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Tombstone.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllTombstonesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Tombstone secondAnnotation = mock(Ord.Tombstone.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Tombstone firstTombstone = new Tombstone().withOrdId("tombstone-1");
    Tombstone secondTombstone = new Tombstone().withOrdId("tombstone-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Tombstone, Tombstone>create(Ord.Tombstone.class))
        .thenReturn(entityGenerator);
    when(tombstoneAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Tombstone.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), tombstoneAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(tombstoneAnnotation, getClass(), document)))
        .thenReturn(firstTombstone);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondTombstone);

    classUnderTest.process(documents);

    assertThat(document.getTombstones()).containsExactly(firstTombstone, secondTombstone);

    verify(customizer).customize(tombstoneAnnotation, document);
    verify(customizer).customize(secondAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }
}
