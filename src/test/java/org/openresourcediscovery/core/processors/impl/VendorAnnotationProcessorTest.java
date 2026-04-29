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
import org.openresourcediscovery.model.Vendor;

@ExtendWith(MockitoExtension.class)
class VendorAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Vendor, Vendor> entityGenerator;

  @Mock
  private Ord.Vendor vendorAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private VendorAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new VendorAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenVendorsAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Vendor generatedVendor = new Vendor().withOrdId("vendor-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(vendorAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), vendorAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(vendorAnnotation, getClass(), document)))
        .thenReturn(generatedVendor);

    classUnderTest.process(documents);

    assertThat(document.getVendors()).containsExactly(generatedVendor);
  }

  @Test
  void givenDocumentAlreadyHasVendors_whenProcessIsCalled_thenNewVendorIsAppended() {
    Vendor existingVendor = new Vendor().withOrdId("existing-vendor");
    Vendor newVendor = new Vendor().withOrdId("new-vendor");
    DocumentSchema document = new DocumentSchema();
    document.setVendors(List.of(existingVendor));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(vendorAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), vendorAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(vendorAnnotation, getClass(), document)))
        .thenReturn(newVendor);

    classUnderTest.process(documents);

    assertThat(document.getVendors()).containsExactly(existingVendor, newVendor);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentVendorsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getVendors()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Vendor.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Vendor.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForVendorAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Vendor.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllVendorsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Vendor secondAnnotation = mock(Ord.Vendor.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Vendor firstVendor = new Vendor().withOrdId("vendor-1");
    Vendor secondVendor = new Vendor().withOrdId("vendor-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Vendor, Vendor>create(Ord.Vendor.class))
        .thenReturn(entityGenerator);
    when(vendorAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Vendor.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), vendorAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(vendorAnnotation, getClass(), document)))
        .thenReturn(firstVendor);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondVendor);

    classUnderTest.process(documents);

    assertThat(document.getVendors()).containsExactly(firstVendor, secondVendor);
  }
}
