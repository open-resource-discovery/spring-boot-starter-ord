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

@ExtendWith(MockitoExtension.class)
class PackageAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Package, org.openresourcediscovery.model.Package> entityGenerator;

  @Mock
  private Ord.Package packageAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private PackageAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new PackageAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenPackagesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    org.openresourcediscovery.model.Package generatedPackage =
        new org.openresourcediscovery.model.Package().withOrdId("package-1");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(packageAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Package.class)).thenReturn(List.of(Pair.of(getClass(), packageAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(packageAnnotation, getClass(), document)))
        .thenReturn(generatedPackage);

    classUnderTest.process(documents);

    assertThat(document.getPackages()).containsExactly(generatedPackage);
  }

  @Test
  void givenDocumentAlreadyHasPackages_whenProcessIsCalled_thenNewPackageIsAppended() {
    org.openresourcediscovery.model.Package existingPackage =
        new org.openresourcediscovery.model.Package().withOrdId("existing-package");
    org.openresourcediscovery.model.Package newPackage =
        new org.openresourcediscovery.model.Package().withOrdId("new-package");
    DocumentSchema document = new DocumentSchema();
    document.setPackages(List.of(existingPackage));

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(packageAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Package.class)).thenReturn(List.of(Pair.of(getClass(), packageAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(packageAnnotation, getClass(), document)))
        .thenReturn(newPackage);

    classUnderTest.process(documents);

    assertThat(document.getPackages()).containsExactly(existingPackage, newPackage);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentPackagesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Package.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getPackages()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Package.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Package.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Package.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForPackageAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Package.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Package.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllPackagesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Package secondAnnotation = mock(Ord.Package.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    org.openresourcediscovery.model.Package firstPackage =
        new org.openresourcediscovery.model.Package().withOrdId("package-1");
    org.openresourcediscovery.model.Package secondPackage =
        new org.openresourcediscovery.model.Package().withOrdId("package-2");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Package, org.openresourcediscovery.model.Package>create(Ord.Package.class))
        .thenReturn(entityGenerator);
    when(packageAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Package.class))
        .thenReturn(List.of(Pair.of(getClass(), packageAnnotation), Pair.of(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(packageAnnotation, getClass(), document)))
        .thenReturn(firstPackage);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondPackage);

    classUnderTest.process(documents);

    assertThat(document.getPackages()).containsExactly(firstPackage, secondPackage);
  }
}
