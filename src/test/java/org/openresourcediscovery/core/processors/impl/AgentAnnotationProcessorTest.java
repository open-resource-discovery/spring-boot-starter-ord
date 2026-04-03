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
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner.ScanResult;
import org.openresourcediscovery.model.Agent;
import org.openresourcediscovery.model.DocumentSchema;

@ExtendWith(MockitoExtension.class)
class AgentAnnotationProcessorTest {

  private static final String DOCUMENT_ID = "doc-1";

  @Mock
  private OrdAnnotationsScanner ordAnnotationsScanner;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  @Mock
  private EntityGenerator<Ord.Agent, Agent> entityGenerator;

  @Mock
  private Ord.Agent agentAnnotation;

  @Mock
  private Ord.DocumentReference documentReference;

  private AgentAnnotationProcessor classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new AgentAnnotationProcessor();
    classUnderTest.setOrdAnnotationsScanner(ordAnnotationsScanner);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  void whenProcessIsCalled_thenAgentsAreAddedToDocument() {
    DocumentSchema document = new DocumentSchema();
    Agent generatedAgent = new Agent().withOrdId("agent-1");

    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(agentAnnotation.partOfDocument()).thenReturn(documentReference);
    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(entityGenerator.generate(Context.of(agentAnnotation, getClass(), document)))
        .thenReturn(generatedAgent);
    when(ordAnnotationsScanner.scan(Ord.Agent.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), agentAnnotation)));

    classUnderTest.process(Map.of(DOCUMENT_ID, new DetectionResult(document, Set.of("open"))));

    assertThat(document.getAgents()).containsExactly(generatedAgent);
  }

  @Test
  void givenDocumentAlreadyHasAgents_whenProcessIsCalled_thenNewAgentIsAppended() {
    Agent existingAgent = new Agent().withOrdId("existing-agent");
    Agent newAgent = new Agent().withOrdId("new-agent");
    DocumentSchema document = new DocumentSchema();
    document.setAgents(List.of(existingAgent));

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(agentAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Agent.class))
        .thenReturn(List.of(new ScanResult<>(getClass(), agentAnnotation)));
    when(entityGenerator.generate(Context.of(agentAnnotation, getClass(), document)))
        .thenReturn(newAgent);

    classUnderTest.process(documents);

    assertThat(document.getAgents()).containsExactly(existingAgent, newAgent);
  }

  @Test
  void givenNoAnnotationsFound_whenProcessIsCalled_thenDocumentAgentsRemainUnchanged() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Agent.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    assertThat(document.getAgents()).isNullOrEmpty();
    verify(ordAnnotationsScanner).scan(Ord.Agent.class);
  }

  @Test
  void whenProcessIsCalled_thenAnnotationsScannerIsCalledWithCorrectPackages() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Agent.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(ordAnnotationsScanner).scan(Ord.Agent.class);
  }

  @Test
  void whenProcessIsCalled_thenEntityGeneratorIsCreatedForAgentAnnotation() {
    DocumentSchema document = new DocumentSchema();

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(ordAnnotationsScanner.scan(Ord.Agent.class)).thenReturn(List.of());

    classUnderTest.process(documents);

    verify(entityGeneratorFactory).create(Ord.Agent.class);
  }

  @Test
  void givenMultipleAnnotations_whenProcessIsCalled_thenAllAgentsAreAdded() {
    DocumentSchema document = new DocumentSchema();
    Ord.Agent secondAnnotation = mock(Ord.Agent.class);
    Ord.DocumentReference secondDocRef = mock(Ord.DocumentReference.class);
    Agent firstAgent = new Agent().withOrdId("agent-1");
    Agent secondAgent = new Agent().withOrdId("agent-2");

    Map<String, DetectionResult> documents = new HashMap<>();
    documents.put(DOCUMENT_ID, new DetectionResult(document, Set.of("open")));

    when(entityGeneratorFactory.<Ord.Agent, Agent>create(Ord.Agent.class)).thenReturn(entityGenerator);
    when(agentAnnotation.partOfDocument()).thenReturn(documentReference);
    when(documentReference.id()).thenReturn(DOCUMENT_ID);
    when(secondAnnotation.partOfDocument()).thenReturn(secondDocRef);
    when(secondDocRef.id()).thenReturn(DOCUMENT_ID);
    when(ordAnnotationsScanner.scan(Ord.Agent.class))
        .thenReturn(List.of(
            new ScanResult<>(getClass(), agentAnnotation),
            new ScanResult<>(String.class, secondAnnotation)));
    when(entityGenerator.generate(Context.of(agentAnnotation, getClass(), document)))
        .thenReturn(firstAgent);
    when(entityGenerator.generate(Context.of(secondAnnotation, String.class, document)))
        .thenReturn(secondAgent);

    classUnderTest.process(documents);

    assertThat(document.getAgents()).containsExactly(firstAgent, secondAgent);
  }
}
