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
import org.openresourcediscovery.model.Product;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ProductAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Product, Product> entityGenerator;

  @Mock
  private Ord.Product productAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.Product> customizer;

  private ProductAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ProductAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenProductsAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Product generatedProduct = new Product().withOrdId("product-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Product.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), productAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(generatedProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(generatedProduct);

    verify(customizer).customize(productAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenDocumentAlreadyHasProducts_whenProcessIsCalled_thenNewProductIsAppended() {
    Product existingProduct = new Product().withOrdId("existing-product");
    Product newProduct = new Product().withOrdId("new-product");
    DocumentSchema document = new DocumentSchema();
    document.setProducts(List.of(existingProduct));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Product.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), productAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(newProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(existingProduct, newProduct);

    verify(customizer).customize(productAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentProductsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getProducts()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Product.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Product.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForProductAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Product.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllProductsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Product secondAnnotation = mock(Ord.Product.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Product firstProduct = new Product().withOrdId("product-1");
    Product secondProduct = new Product().withOrdId("product-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.Product.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), productAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(firstProduct);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(firstProduct, secondProduct);

    verify(customizer).customize(productAnnotation, document);
    verify(customizer).customize(secondAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }
}
