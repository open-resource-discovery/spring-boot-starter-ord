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
import org.openresourcediscovery.model.Group;

@ExtendWith(MockitoExtension.class)
class GroupAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Group, Group> entityGenerator;

  @Mock
  private Ord.Group groupAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private GroupAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new GroupAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenGroupsAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Group generatedGroup = new Group().withGroupId("group-1");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(groupAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Group.class)).thenReturn(List.of(Pair.of(getClass(), groupAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupAnnotation, getClass(), document)))
        .thenReturn(generatedGroup);

    classUnderTest.process(documents);

    assertThat(document.getGroups()).containsExactly(generatedGroup);
  }

  @Test
  void givenDocumentAlreadyHasGroups_whenProcessIsCalled_thenNewGroupIsAppended() {
    Group existingGroup = new Group().withGroupId("existing-group");
    Group newGroup = new Group().withGroupId("new-group");
    DocumentSchema document = new DocumentSchema();
    document.setGroups(List.of(existingGroup));

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(groupAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Group.class)).thenReturn(List.of(Pair.of(getClass(), groupAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupAnnotation, getClass(), document)))
        .thenReturn(newGroup);

    classUnderTest.process(documents);

    assertThat(document.getGroups()).containsExactly(existingGroup, newGroup);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentGroupsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Group.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getGroups()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Group.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Group.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Group.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForGroupAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Group.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Group.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllGroupsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Group secondAnnotation = mock(Ord.Group.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Group firstGroup = new Group().withGroupId("group-1");
    Group secondGroup = new Group().withGroupId("group-2");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Group, Group>create(Ord.Group.class)).thenReturn(entityGenerator);
    when(groupAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Group.class))
        .thenReturn(List.of(Pair.of(getClass(), groupAnnotation), Pair.of(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(groupAnnotation, getClass(), document)))
        .thenReturn(firstGroup);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondGroup);

    classUnderTest.process(documents);

    assertThat(document.getGroups()).containsExactly(firstGroup, secondGroup);
  }
}
