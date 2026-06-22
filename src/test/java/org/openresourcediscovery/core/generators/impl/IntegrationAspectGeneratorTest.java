package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.openresourcediscovery.model.ApiResourceIntegrationAspect;
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.CapabilityIntegrationAspect;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EventResourceIntegrationAspect;
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.IntegrationAspect;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class IntegrationAspectGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.IntegrationAspect, IntegrationAspect> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private IntegrationAspectGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new IntegrationAspectGenerator();

    classUnderTest.setCustomizers(List.of(customizer));
    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(
        Ord.ApiResourceIntegrationAspect.class, new EntityAutoGenerator<>(ApiResourceIntegrationAspect::new));
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceIntegrationAspect.class,
        new EntityAutoGenerator<>(EventResourceIntegrationAspect::new));
    prepareEntityGeneratorFactoryMock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(ApiResourceIntegrationAspectSubset::new));
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(EventResourceIntegrationAspectSubset::new));
    prepareEntityGeneratorFactoryMock(
        Ord.CapabilityIntegrationAspect.class, new EntityAutoGenerator<>(CapabilityIntegrationAspect::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(9, Ord.IntegrationAspect.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.IntegrationAspect> context =
        Context.of(Annotations.mock(Ord.IntegrationAspect.class), getClass(), new DocumentSchema());

    assertEquals(
        new IntegrationAspect()
            .withMandatory(false)
            .withSupportMultipleProviders(false)
            .withTitle(getClass().getSimpleName()),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.IntegrationAspect annotation = Annotations.mock(
        Ord.IntegrationAspect.class,
        Map.ofEntries(
            Map.entry("mandatory", true),
            Map.entry("title", "test-aspect-title"),
            Map.entry("supportMultipleProviders", true),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("description", "test-aspect-description"),
            Map.entry("apiResources", new Ord.ApiResourceIntegrationAspect[] {
              createApiResourceIntegrationAspectAnnotationMock()
            }),
            Map.entry("eventResources", new Ord.EventResourceIntegrationAspect[] {
              createEventResourceIntegrationAspectAnnotationMock()
            }),
            Map.entry("capabilities", new Ord.CapabilityIntegrationAspect[] {
              createCapabilityIntegrationAspectAnnotationMock()
            })));
    Context<Ord.IntegrationAspect> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new IntegrationAspect()
            .withMandatory(true)
            .withTitle("test-aspect-title")
            .withSupportMultipleProviders(true)
            .withDescription("test-aspect-description")
            .withApiResources(List.of(new ApiResourceIntegrationAspect()
                .withMinVersion("1.0.0")
                .withOrdId("test-api-resource-ord-id")
                .withSubset(List.of(new ApiResourceIntegrationAspectSubset()
                    .withOperationId("test-operation-id")))))
            .withEventResources(List.of(new EventResourceIntegrationAspect()
                .withMinVersion("1.0.0")
                .withOrdId("test-event-resource-ord-id")
                .withSystemTypeRestriction(
                    List.of("test-system-type-restriction-1", "test-system-type-restriction-2"))
                .withSubset(List.of(
                    new EventResourceIntegrationAspectSubset().withEventType("test-event-type")))))
            .withCapabilities(List.of(new CapabilityIntegrationAspect()
                .withOrdId("test-capability-ord-id")
                .withMinVersion("1.0.0")))
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

  private static Ord.ApiResourceIntegrationAspect createApiResourceIntegrationAspectAnnotationMock() {
    return Annotations.mock(
        Ord.ApiResourceIntegrationAspect.class,
        Map.ofEntries(
            Map.entry("minVersion", "1.0.0"),
            Map.entry("ordId", "test-api-resource-ord-id"),
            Map.entry("subset", new Ord.ApiResourceIntegrationAspectSubset[] {
              Annotations.mock(
                  Ord.ApiResourceIntegrationAspectSubset.class,
                  Map.ofEntries(Map.entry("operationId", "test-operation-id")))
            })));
  }

  private static Ord.EventResourceIntegrationAspect createEventResourceIntegrationAspectAnnotationMock() {
    return Annotations.mock(
        Ord.EventResourceIntegrationAspect.class,
        Map.ofEntries(
            Map.entry("minVersion", "1.0.0"),
            Map.entry("ordId", "test-event-resource-ord-id"),
            Map.entry(
                "systemTypeRestriction",
                new String[] {"test-system-type-restriction-1", "test-system-type-restriction-2"}),
            Map.entry("subset", new Ord.EventResourceIntegrationAspectSubset[] {
              Annotations.mock(
                  Ord.EventResourceIntegrationAspectSubset.class,
                  Map.ofEntries(Map.entry("eventType", "test-event-type")))
            })));
  }

  private static Ord.CapabilityIntegrationAspect createCapabilityIntegrationAspectAnnotationMock() {
    return Annotations.mock(
        Ord.CapabilityIntegrationAspect.class,
        Map.ofEntries(Map.entry("ordId", "test-capability-ord-id"), Map.entry("minVersion", "1.0.0")));
  }
}
