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
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner.ScanResult;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Overlay;

@ExtendWith(MockitoExtension.class)
class OverlayAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Overlay, Overlay> entityGenerator;

  @Mock
  private Ord.Overlay overlayAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private OverlayAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new OverlayAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenOverlaysAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Overlay generatedOverlay = new Overlay().withOrdId("overlay-1");

    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(overlayAnnotation.partOfDocument()).thenReturn(documentReference);
    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(entityGenerator.generate(Context.of(overlayAnnotation, getClass(), document)))
        .thenReturn(generatedOverlay);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), overlayAnnotation)));

    classUnderTest.process(Map.of(DOCUMENT_NAME, new DetectionResult(document, Set.of("open"))));

    assertThat(document.getOverlays()).containsExactly(generatedOverlay);
  }

  @Test
  void givenDocumentAlreadyHasOverlays_whenProcessIsCalled_thenNewOverlayIsAppended() {
    Overlay existingOverlay = new Overlay().withOrdId("existing-overlay");
    Overlay newOverlay = new Overlay().withOrdId("new-overlay");
    DocumentSchema document = new DocumentSchema();
    document.setOverlays(List.of(existingOverlay));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(overlayAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), overlayAnnotation)));
    when(entityGenerator.generate(Context.of(overlayAnnotation, getClass(), document)))
        .thenReturn(newOverlay);

    classUnderTest.process(documents);

    assertThat(document.getOverlays()).containsExactly(existingOverlay, newOverlay);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentOverlaysRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getOverlays()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Overlay.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Overlay.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForOverlayAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Overlay.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllOverlaysAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Overlay secondAnnotation = mock(Ord.Overlay.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Overlay firstOverlay = new Overlay().withOrdId("overlay-1");
    Overlay secondOverlay = new Overlay().withOrdId("overlay-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Overlay, Overlay>create(Ord.Overlay.class))
        .thenReturn(entityGenerator);
    when(overlayAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Overlay.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), overlayAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(Context.of(overlayAnnotation, getClass(), document)))
        .thenReturn(firstOverlay);
    when(entityGenerator.generate(Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondOverlay);

    classUnderTest.process(documents);

    assertThat(document.getOverlays()).containsExactly(firstOverlay, secondOverlay);
  }
}
