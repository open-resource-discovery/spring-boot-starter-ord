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
import org.openresourcediscovery.model.AccessStrategy;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Overlay;
import org.openresourcediscovery.model.OverlayDefinition;
import org.openresourcediscovery.model.RelatedApiResource;
import org.openresourcediscovery.model.RelatedEventResource;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class OverlayGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.Overlay, Overlay> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private OverlayGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new OverlayGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new));
    prepareEntityGeneratorFactoryMock(
        Ord.OverlayDefinition.class, new EntityAutoGenerator<>(OverlayDefinition::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedApiResource.class, new EntityAutoGenerator<>(RelatedApiResource::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedEventResource.class, new EntityAutoGenerator<>(RelatedEventResource::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(14, Ord.Overlay.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.Overlay> context =
        Context.of(Annotations.mock(Ord.Overlay.class), getClass(), new DocumentSchema());

    assertEquals(
        new Overlay()
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withOrdId("%s:overlay:%s:v1"
                .formatted(NAMESPACE, getClass().getSimpleName())),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Overlay annotation = Annotations.mock(
        Ord.Overlay.class,
        Map.ofEntries(
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("visibility", "private"),
            Map.entry("releaseStatus", "beta"),
            Map.entry("description", "Custom description"),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("ordId", NAMESPACE + ":overlay:Custom:v1"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("definitions", new Ord.OverlayDefinition[] {createOverlayDefinitionAnnotationMock()}),
            Map.entry(
                "relatedApiResources",
                new Ord.RelatedApiResource[] {createRelatedApiResourceAnnotationMock()}),
            Map.entry(
                "relatedEventResources",
                new Ord.RelatedEventResource[] {createRelatedEventResourceAnnotationMock()})));
    Context<Ord.Overlay> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Overlay()
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withVisibility("private")
            .withReleaseStatus("beta")
            .withDescription("Custom description")
            .withTags(List.of("tag-1", "tag-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withOrdId(NAMESPACE + ":overlay:Custom:v1")
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withDefinitions(List.of(new OverlayDefinition()
                .withVisibility("public")
                .withType("openapi-overlay")
                .withPurpose("ord:ai-enrichment")
                .withMediaType("application/yaml")
                .withUrl("https://test-overlay-definition.dummy.nowhere.org")
                .withAccessStrategies(List.of(new AccessStrategy()
                    .withType("open")
                    .withCustomType("test-access-strategy-custom-type")
                    .withCustomDescription("test-access-strategy-custom-description")))))
            .withRelatedApiResources(List.of(new RelatedApiResource()
                .withOrdId("test-related-api-resource-ord-id")
                .withRelationType("test-relation-type")))
            .withRelatedEventResources(List.of(new RelatedEventResource()
                .withOrdId("test-related-event-resource-ord-id")
                .withRelationType("test-relation-type"))),
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

  private static Ord.AccessStrategy createAccessStrategyAnnotationMock() {
    return Annotations.mock(
        Ord.AccessStrategy.class,
        Map.ofEntries(
            Map.entry("type", "open"),
            Map.entry("customType", "test-access-strategy-custom-type"),
            Map.entry("customDescription", "test-access-strategy-custom-description")));
  }

  private static Ord.OverlayDefinition createOverlayDefinitionAnnotationMock() {
    return Annotations.mock(
        Ord.OverlayDefinition.class,
        Map.ofEntries(
            Map.entry("visibility", "public"),
            Map.entry("type", "openapi-overlay"),
            Map.entry("purpose", "ord:ai-enrichment"),
            Map.entry("mediaType", "application/yaml"),
            Map.entry("url", "https://test-overlay-definition.dummy.nowhere.org"),
            Map.entry(
                "accessStrategies", new Ord.AccessStrategy[] {createAccessStrategyAnnotationMock()})));
  }

  private static Ord.RelatedApiResource createRelatedApiResourceAnnotationMock() {
    return Annotations.mock(
        Ord.RelatedApiResource.class,
        Map.ofEntries(
            Map.entry("ordId", "test-related-api-resource-ord-id"),
            Map.entry("relationType", "test-relation-type")));
  }

  private static Ord.RelatedEventResource createRelatedEventResourceAnnotationMock() {
    return Annotations.mock(
        Ord.RelatedEventResource.class,
        Map.ofEntries(
            Map.entry("ordId", "test-related-event-resource-ord-id"),
            Map.entry("relationType", "test-relation-type")));
  }
}
