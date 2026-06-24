package org.openresourcediscovery.core.processors.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.openresourcediscovery.model.SystemInstance;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class SystemInstanceAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.SystemInstance, SystemInstance> entityGenerator;

  @Mock
  private Ord.SystemInstance systemInstanceAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  @Mock
  private AnnotationProcessor.Customizer<Ord.SystemInstance> customizer;

  private SystemInstanceAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new SystemInstanceAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));
  }

  @Test
  void whenProcessIsCalled_thenSystemInstanceIsSetOnDocument() {
    DocumentSchema document = new DocumentSchema();
    SystemInstance generatedInstance = new SystemInstance();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemInstance, SystemInstance>create(Ord.SystemInstance.class))
        .thenReturn(entityGenerator);
    when(systemInstanceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemInstance.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), systemInstanceAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(systemInstanceAnnotation, getClass(), document)))
        .thenReturn(generatedInstance);

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemInstance()).isSameAs(generatedInstance);

    verify(customizer).customize(systemInstanceAnnotation, document);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDescribedSystemInstanceIsNull() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemInstance, SystemInstance>create(Ord.SystemInstance.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemInstance.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemInstance()).isNull();
    verify(ordAnnotationsScanner).scan(Ord.SystemInstance.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemInstance, SystemInstance>create(Ord.SystemInstance.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemInstance.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.SystemInstance.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForSystemInstanceAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemInstance, SystemInstance>create(Ord.SystemInstance.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemInstance.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.SystemInstance.class);
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenExistingSystemInstance_whenProcessIsCalled_thenExceptionIsThrown() {
    DocumentSchema document = new DocumentSchema();
    SystemInstance existingInstance = new SystemInstance();
    document.setDescribedSystemInstance(existingInstance);

    Ord.SystemInstance secondAnnotation = mock(Ord.SystemInstance.class);

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemInstance, SystemInstance>create(Ord.SystemInstance.class))
        .thenReturn(entityGenerator);
    when(systemInstanceAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemInstance.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), systemInstanceAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));

    assertThrows(IllegalStateException.class, () -> classUnderTest.process(documents));
  }
}
