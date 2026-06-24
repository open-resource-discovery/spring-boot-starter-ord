package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EventResourceIntegrationAspect;
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class EventResourceIntegrationAspectGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.EventResourceIntegrationAspect, EventResourceIntegrationAspect> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityAutoGenerator<Ord.EventResourceIntegrationAspect, EventResourceIntegrationAspect> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(EventResourceIntegrationAspect::new) {};

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(EventResourceIntegrationAspectSubset::new) {});
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(6, Ord.EventResourceIntegrationAspect.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.EventResourceIntegrationAspect.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventResourceIntegrationAspect annotation = Annotations.mock(
        Ord.EventResourceIntegrationAspect.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":eventResource:Test:v1")));
    Context<Ord.EventResourceIntegrationAspect> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EventResourceIntegrationAspect().withOrdId(NAMESPACE + ":eventResource:Test:v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventResourceIntegrationAspect annotation = Annotations.mock(
        Ord.EventResourceIntegrationAspect.class,
        Map.ofEntries(
            Map.entry("minVersion", "1.0.0"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("ordId", NAMESPACE + ":eventResource:Test:v1"),
            Map.entry("systemTypeRestriction", new String[] {"test-1", "test-2"}),
            Map.entry("subset", new Ord.EventResourceIntegrationAspectSubset[] {createSubsetAnnotationMock()
            })));
    Context<Ord.EventResourceIntegrationAspect> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EventResourceIntegrationAspect()
            .withMinVersion("1.0.0")
            .withOrdId(NAMESPACE + ":eventResource:Test:v1")
            .withSystemTypeRestriction(List.of("test-1", "test-2"))
            .withSubset(
                List.of(new EventResourceIntegrationAspectSubset().withEventType("test-event-type")))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2"))),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.Labels createLabelsAnnotationMock() {
    return Annotations.mock(Ord.Labels.class, Map.of("value", new Ord.LabelsEntry[] {
      Annotations.mock(
          Ord.LabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-label-key"),
              Map.entry("values", new String[] {"test-label-value-1", "test-label-value-2"})))
    }));
  }

  private static Ord.EventResourceIntegrationAspectSubset createSubsetAnnotationMock() {
    return Annotations.mock(
        Ord.EventResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("eventType", "test-event-type")));
  }
}
