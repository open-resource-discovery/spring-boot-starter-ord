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
import org.openresourcediscovery.model.SystemVersion;

@ExtendWith(MockitoExtension.class)
class SystemVersionAnnotationProcessorTest {

  private static final String DOCUMENT_NAME = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.SystemVersion, SystemVersion> entityGenerator;

  @Mock
  private Ord.SystemVersion systemVersionAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private SystemVersionAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new SystemVersionAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenSystemVersionIsSetOnDocument() {
    DocumentSchema document = new DocumentSchema();
    SystemVersion generatedSystemVersion = new SystemVersion();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemVersion, SystemVersion>create(Ord.SystemVersion.class))
        .thenReturn(entityGenerator);
    when(systemVersionAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemVersion.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), systemVersionAnnotation)));
    when(entityGenerator.generate(EntityGenerator.Context.of(systemVersionAnnotation, getClass(), document)))
        .thenReturn(generatedSystemVersion);

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemVersion()).isSameAs(generatedSystemVersion);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDescribedSystemVersionIsNull() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemVersion, SystemVersion>create(Ord.SystemVersion.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemVersion.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getDescribedSystemVersion()).isNull();
    verify(ordAnnotationsScanner).scan(Ord.SystemVersion.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemVersion, SystemVersion>create(Ord.SystemVersion.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemVersion.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.SystemVersion.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForSystemVersionAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemVersion, SystemVersion>create(Ord.SystemVersion.class))
        .thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.SystemVersion.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.SystemVersion.class);
  }

  @Test
  void givenExistingSystemVersion_whenProcessIsCalled_thenSystemVersionIsOverwritten() {
    DocumentSchema document = new DocumentSchema();
    SystemVersion existingSystemVersion = new SystemVersion();
    document.setDescribedSystemVersion(existingSystemVersion);

    Ord.SystemVersion secondAnnotation = mock(Ord.SystemVersion.class);

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_NAME, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.SystemVersion, SystemVersion>create(Ord.SystemVersion.class))
        .thenReturn(entityGenerator);
    when(systemVersionAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.name()).thenReturn(DOCUMENT_NAME);
    when(ordAnnotationsScanner.scan(Ord.SystemVersion.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), systemVersionAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));

    assertThrows(IllegalStateException.class, () -> classUnderTest.process(documents));
  }
}
