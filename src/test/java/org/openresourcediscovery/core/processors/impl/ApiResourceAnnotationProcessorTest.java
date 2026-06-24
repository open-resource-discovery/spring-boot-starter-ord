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
import org.openresourcediscovery.model.ApiResource;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ApiResourceAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.ApiResource, ApiResource> entityGenerator;

  @Mock
  private Ord.ApiResource apiResourceAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.ApiResource> customizer;

  private ApiResourceAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ApiResourceAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenApiResourcesAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    ApiResource generatedApiResource = new ApiResource().withOrdId("api-resource-1");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(apiResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), apiResourceAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(apiResourceAnnotation, getClass(), document)))
        .thenReturn(generatedApiResource);

    classUnderTest.process(documents);

    assertThat(document.getApiResources()).containsExactly(generatedApiResource);

    verify(customizer).customize(apiResourceAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenDocumentAlreadyHasApiResources_whenProcessIsCalled_thenNewApiResourceIsAppended() {
    ApiResource existingApiResource = new ApiResource().withOrdId("existing-api-resource");
    ApiResource newApiResource = new ApiResource().withOrdId("new-api-resource");
    DocumentSchema document = new DocumentSchema();
    document.setApiResources(List.of(existingApiResource));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(apiResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), apiResourceAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(apiResourceAnnotation, getClass(), document)))
        .thenReturn(newApiResource);

    classUnderTest.process(documents);

    assertThat(document.getApiResources()).containsExactly(existingApiResource, newApiResource);

    verify(customizer).customize(apiResourceAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentApiResourcesRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getApiResources()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.ApiResource.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.ApiResource.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForApiResourceAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.ApiResource.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllApiResourcesAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.ApiResource secondAnnotation = mock(Ord.ApiResource.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    ApiResource firstApiResource = new ApiResource().withOrdId("api-resource-1");
    ApiResource secondApiResource = new ApiResource().withOrdId("api-resource-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.ApiResource, ApiResource>create(Ord.ApiResource.class))
        .thenReturn(entityGenerator);
    when(apiResourceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.ApiResource.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), apiResourceAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(apiResourceAnnotation, getClass(), document)))
        .thenReturn(firstApiResource);
    when(entityGenerator.generate(EntityGenerator.Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondApiResource);

    classUnderTest.process(documents);

    assertThat(document.getApiResources()).containsExactly(firstApiResource, secondApiResource);

    verify(customizer).customize(apiResourceAnnotation, document);
    verify(customizer).customize(secondAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }
}
