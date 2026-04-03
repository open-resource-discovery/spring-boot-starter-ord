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
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DocumentSchema;

@ExtendWith(MockitoExtension.class)
class DataProductAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.DataProduct, DataProduct> entityGenerator;

  @Mock
  private Ord.DataProduct dataProductAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private DataProductAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new DataProductAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenDataProductsAreAddedToDocument() {
    List<String> packages = List.of("org.example");
    DocumentSchema document = new DocumentSchema();
    DataProduct generatedDataProduct = new DataProduct().withOrdId("data-product-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(dataProductAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), dataProductAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(dataProductAnnotation, getClass(), document)))
        .thenReturn(generatedDataProduct);

    classUnderTest.process(documents);

    assertThat(document.getDataProducts()).containsExactly(generatedDataProduct);
  }

  @Test
  void givenDocumentAlreadyHasDataProducts_whenProcessIsCalled_thenNewDataProductIsAppended() {
    DataProduct existingDataProduct = new DataProduct().withOrdId("existing-data-product");
    DataProduct newDataProduct = new DataProduct().withOrdId("new-data-product");
    DocumentSchema document = new DocumentSchema();
    document.setDataProducts(List.of(existingDataProduct));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(dataProductAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), dataProductAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(dataProductAnnotation, getClass(), document)))
        .thenReturn(newDataProduct);

    classUnderTest.process(documents);

    assertThat(document.getDataProducts()).containsExactly(existingDataProduct, newDataProduct);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentDataProductsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getDataProducts()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.DataProduct.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.DataProduct.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForDataProductAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.DataProduct.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllDataProductsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.DataProduct secondAnnotation = mock(Ord.DataProduct.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    DataProduct firstDataProduct = new DataProduct().withOrdId("data-product-1");
    DataProduct secondDataProduct = new DataProduct().withOrdId("data-product-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.DataProduct, DataProduct>create(Ord.DataProduct.class))
        .thenReturn(entityGenerator);
    when(dataProductAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.DataProduct.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), dataProductAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(dataProductAnnotation, getClass(), document)))
        .thenReturn(firstDataProduct);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondDataProduct);

    classUnderTest.process(documents);

    assertThat(document.getDataProducts()).containsExactly(firstDataProduct, secondDataProduct);
  }
}
