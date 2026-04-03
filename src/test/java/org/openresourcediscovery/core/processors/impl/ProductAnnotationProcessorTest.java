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
import org.openresourcediscovery.model.Product;

@ExtendWith(MockitoExtension.class)
class ProductAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

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

  private ProductAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ProductAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenProductsAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Product generatedProduct = new Product().withOrdId("product-1");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of(Pair.of(getClass(), productAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(generatedProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(generatedProduct);
  }

  @Test
  void givenDocumentAlreadyHasProducts_whenProcessIsCalled_thenNewProductIsAppended() {
    Product existingProduct = new Product().withOrdId("existing-product");
    Product newProduct = new Product().withOrdId("new-product");
    DocumentSchema document = new DocumentSchema();
    document.setProducts(List.of(existingProduct));

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of(Pair.of(getClass(), productAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(newProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(existingProduct, newProduct);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentProductsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getProducts()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Product.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Product.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForProductAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Product.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Product.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllProductsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Product secondAnnotation = mock(Ord.Product.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Product firstProduct = new Product().withOrdId("product-1");
    Product secondProduct = new Product().withOrdId("product-2");

    Map<String, Pair<DocumentSchema, Set<String>>> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, Pair.of(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Product, Product>create(Ord.Product.class))
        .thenReturn(entityGenerator);
    when(productAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Product.class))
        .thenReturn(List.of(Pair.of(getClass(), productAnnotation), Pair.of(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(productAnnotation, getClass(), document)))
        .thenReturn(firstProduct);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondProduct);

    classUnderTest.process(documents);

    assertThat(document.getProducts()).containsExactly(firstProduct, secondProduct);
  }
}
