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
import org.openresourcediscovery.model.GroupType;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class GroupTypeAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.GroupType, GroupType> entityGenerator;

  @Mock
  private Ord.GroupType groupTypeAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.GroupType> customizer;

  private GroupTypeAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new GroupTypeAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenGroupTypesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    GroupType generatedGroupType = new GroupType().withGroupTypeId("group-type-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), groupTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(generatedGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(generatedGroupType);

    verify(customizer).customize(groupTypeAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenDocumentAlreadyHasGroupTypes_whenProcessIsCalled_thenNewGroupTypeIsAppended() {
    GroupType existingGroupType = new GroupType().withGroupTypeId("existing-group-type");
    GroupType newGroupType = new GroupType().withGroupTypeId("new-group-type");
    DocumentSchema document = new DocumentSchema();
    document.setGroupTypes(List.of(existingGroupType));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), groupTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(newGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(existingGroupType, newGroupType);

    verify(customizer).customize(groupTypeAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentGroupTypesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.GroupType.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.GroupType.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForGroupTypeAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.GroupType.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllGroupTypesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.GroupType secondAnnotation = mock(Ord.GroupType.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    GroupType firstGroupType = new GroupType().withGroupTypeId("group-type-1");
    GroupType secondGroupType = new GroupType().withGroupTypeId("group-type-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), groupTypeAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(firstGroupType);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(firstGroupType, secondGroupType);

    verify(customizer).customize(groupTypeAnnotation, document);
    verify(customizer).customize(secondAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }
}
