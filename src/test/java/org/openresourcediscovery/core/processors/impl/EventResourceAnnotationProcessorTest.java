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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EventResource;

@ExtendWith(MockitoExtension.class)
class EventResourceAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.EventResource, EventResource> entityGenerator;

  @Mock
  private Ord.EventResource eventResourceAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private EventResourceAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EventResourceAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenEventResourcesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    EventResource generatedEventResource = new EventResource().withOrdId("event-resource-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(eventResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), eventResourceAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(eventResourceAnnotation, getClass(), document)))
        .thenReturn(generatedEventResource);

    classUnderTest.process(documents);

    assertThat(document.getEventResources()).containsExactly(generatedEventResource);
  }

  @Test
  void givenDocumentAlreadyHasEventResources_whenProcessIsCalled_thenNewEventResourceIsAppended() {
    EventResource existingEventResource = new EventResource().withOrdId("existing-event-resource");
    EventResource newEventResource = new EventResource().withOrdId("new-event-resource");
    DocumentSchema document = new DocumentSchema();
    document.setEventResources(List.of(existingEventResource));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(eventResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), eventResourceAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(eventResourceAnnotation, getClass(), document)))
        .thenReturn(newEventResource);

    classUnderTest.process(documents);

    assertThat(document.getEventResources()).containsExactly(existingEventResource, newEventResource);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentEventResourcesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getEventResources()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.EventResource.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.EventResource.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForEventResourceAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.EventResource.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllEventResourcesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.EventResource secondAnnotation = mock(Ord.EventResource.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    EventResource firstEventResource = new EventResource().withOrdId("event-resource-1");
    EventResource secondEventResource = new EventResource().withOrdId("event-resource-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EventResource, EventResource>create(Ord.EventResource.class))
        .thenReturn(entityGenerator);
    when(eventResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.EventResource.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), eventResourceAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(eventResourceAnnotation, getClass(), document)))
        .thenReturn(firstEventResource);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondEventResource);

    classUnderTest.process(documents);

    assertThat(document.getEventResources()).containsExactly(firstEventResource, secondEventResource);
  }
}
