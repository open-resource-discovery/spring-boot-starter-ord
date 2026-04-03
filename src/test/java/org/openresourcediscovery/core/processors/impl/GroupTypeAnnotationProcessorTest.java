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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.GroupType;

@ExtendWith(MockitoExtension.class)
class GroupTypeAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

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

  private GroupTypeAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new GroupTypeAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenGroupTypesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    GroupType generatedGroupType = new GroupType().withGroupTypeId("group-type-1");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(Pair.of(getClass(), groupTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(generatedGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(generatedGroupType);
  }

  @Test
  void givenDocumentAlreadyHasGroupTypes_whenProcessIsCalled_thenNewGroupTypeIsAppended() {
    GroupType existingGroupType = new GroupType().withGroupTypeId("existing-group-type");
    GroupType newGroupType = new GroupType().withGroupTypeId("new-group-type");
    DocumentSchema document = new DocumentSchema();
    document.setGroupTypes(List.of(existingGroupType));

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(Pair.of(getClass(), groupTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(newGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(existingGroupType, newGroupType);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentGroupTypesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.GroupType.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.GroupType.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForGroupTypeAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.GroupType.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllGroupTypesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.GroupType secondAnnotation = mock(Ord.GroupType.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    GroupType firstGroupType = new GroupType().withGroupTypeId("group-type-1");
    GroupType secondGroupType = new GroupType().withGroupTypeId("group-type-2");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.GroupType, GroupType>create(Ord.GroupType.class))
        .thenReturn(entityGenerator);
    when(groupTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.GroupType.class))
        .thenReturn(List.of(Pair.of(getClass(), groupTypeAnnotation), Pair.of(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupTypeAnnotation, getClass(), document)))
        .thenReturn(firstGroupType);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondGroupType);

    classUnderTest.process(documents);

    assertThat(document.getGroupTypes()).containsExactly(firstGroupType, secondGroupType);
  }
}
