package org.openresourcediscovery.core.processors.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.openresourcediscovery.model.SystemType;

@ExtendWith(MockitoExtension.class)
class SystemTypeAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.SystemType, SystemType> entityGenerator;

  @Mock
  private Ord.SystemType systemTypeAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private SystemTypeAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new SystemTypeAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenSystemTypeIsSetOnDocument() {
    DocumentSchema document = new DocumentSchema();
    SystemType generatedSystemType = new SystemType();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemType, SystemType>create(Ord.SystemType.class))
        .thenReturn(entityGenerator);
    when(systemTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemType.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), systemTypeAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(systemTypeAnnotation, getClass(), document)))
        .thenReturn(generatedSystemType);

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemType()).isSameAs(generatedSystemType);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDescribedSystemTypeIsNull() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemType, SystemType>create(Ord.SystemType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemType()).isNull();
    verify(ordAnnotationsScanner).scan(Ord.SystemType.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemType, SystemType>create(Ord.SystemType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.SystemType.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForSystemTypeAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemType, SystemType>create(Ord.SystemType.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemType.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.SystemType.class);
  }

  @Test
  void givenExistingSystemType_whenProcessIsCalled_thenSystemTypeIsOverwritten() {
    DocumentSchema document = new DocumentSchema();
    SystemType existingSystemType = new SystemType();
    document.setDescribedSystemType(existingSystemType);

    Ord.SystemType secondAnnotation = mock(Ord.SystemType.class);

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemType, SystemType>create(Ord.SystemType.class))
        .thenReturn(entityGenerator);
    when(systemTypeAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemType.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), systemTypeAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));

    assertThrows(IllegalStateException.class, () -> classUnderTest.process(documents));
  }
}
