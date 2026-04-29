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
import org.openresourcediscovery.model.EntityType;

@ExtendWith(MockitoExtension.class)
class EntityTypeAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.EntityType, EntityType> entityGenerator;

  @Mock
  private Ord.EntityType entityTypeAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private EntityTypeAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityTypeAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenEntityTypesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    EntityType generatedEntityType = new EntityType().withOrdId("entity-type-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(entityTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), entityTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(entityTypeAnnotation, getClass(), document)))
        .thenReturn(generatedEntityType);

    classUnderTest.process(documents);

    assertThat(document.getEntityTypes()).containsExactly(generatedEntityType);
  }

  @Test
  void givenDocumentAlreadyHasEntityTypes_whenProcessIsCalled_thenNewEntityTypeIsAppended() {
    EntityType existingEntityType = new EntityType().withOrdId("existing-entity-type");
    EntityType newEntityType = new EntityType().withOrdId("new-entity-type");
    DocumentSchema document = new DocumentSchema();
    document.setEntityTypes(List.of(existingEntityType));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(entityTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), entityTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(entityTypeAnnotation, getClass(), document)))
        .thenReturn(newEntityType);

    classUnderTest.process(documents);

    assertThat(document.getEntityTypes()).containsExactly(existingEntityType, newEntityType);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentEntityTypesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getEntityTypes()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.EntityType.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.EntityType.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForEntityTypeAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.EntityType.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllEntityTypesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.EntityType secondAnnotation = mock(Ord.EntityType.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    EntityType firstEntityType = new EntityType().withOrdId("entity-type-1");
    EntityType secondEntityType = new EntityType().withOrdId("entity-type-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.EntityType, EntityType>create(Ord.EntityType.class))
        .thenReturn(entityGenerator);
    when(entityTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.EntityType.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), entityTypeAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(entityTypeAnnotation, getClass(), document)))
        .thenReturn(firstEntityType);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondEntityType);

    classUnderTest.process(documents);

    assertThat(document.getEntityTypes()).containsExactly(firstEntityType, secondEntityType);
  }
}
