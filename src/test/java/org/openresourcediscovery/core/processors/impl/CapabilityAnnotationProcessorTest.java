package org.openresourcediscovery.core.processors.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.Capability;
import org.openresourcediscovery.model.DocumentSchema;

@ExtendWith(MockitoExtension.class)
class CapabilityAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Capability, Capability> entityGenerator;

  @Mock
  private Ord.Capability capabilityAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private CapabilityAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new CapabilityAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenCapabilitiesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Capability generatedCapability = new Capability().withOrdId("capability-1");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(capabilityAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Capability.class))
        .thenReturn(List.of(Pair.of(getClass(), capabilityAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(capabilityAnnotation, getClass(), document)))
        .thenReturn(generatedCapability);

    classUnderTest.process(documents);

    assertThat(document.getCapabilities()).containsExactly(generatedCapability);
  }

  @Test
  void givenDocumentAlreadyHasCapabilities_whenProcessIsCalled_thenNewCapabilityIsAppended() {
    Capability existingCapability = new Capability().withOrdId("existing-capability");
    Capability newCapability = new Capability().withOrdId("new-capability");
    DocumentSchema document = new DocumentSchema();
    document.setCapabilities(List.of(existingCapability));

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(capabilityAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Capability.class))
        .thenReturn(List.of(Pair.of(getClass(), capabilityAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(capabilityAnnotation, getClass(), document)))
        .thenReturn(newCapability);

    classUnderTest.process(documents);

    assertThat(document.getCapabilities()).containsExactly(existingCapability, newCapability);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentCapabilitiesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Capability.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getCapabilities()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Capability.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Capability.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Capability.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForCapabilityAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Capability.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Capability.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllCapabilitiesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Capability secondAnnotation = mock(Ord.Capability.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Capability firstCapability = new Capability().withOrdId("capability-1");
    Capability secondCapability = new Capability().withOrdId("capability-2");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Capability, Capability>create(Ord.Capability.class))
        .thenReturn(entityGenerator);
    when(capabilityAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Capability.class))
        .thenReturn(
            List.of(Pair.of(getClass(), capabilityAnnotation), Pair.of(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(capabilityAnnotation, getClass(), document)))
        .thenReturn(firstCapability);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondCapability);

    classUnderTest.process(documents);

    assertThat(document.getCapabilities()).containsExactly(firstCapability, secondCapability);
  }
}
