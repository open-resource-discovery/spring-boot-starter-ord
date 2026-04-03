package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.lang.annotation.Annotation;
import java.net.URI;
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
import org.openresourcediscovery.model.Capability;
import org.openresourcediscovery.model.CapabilityDefinition;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.RelatedApiResource;
import org.openresourcediscovery.model.RelatedCapability;
import org.openresourcediscovery.model.RelatedEventResource;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class CapabilityGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private CapabilityGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new CapabilityGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new));
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new));
    prepareEntityGeneratorFactoryMock(
        Ord.CapabilityDefinition.class, new EntityAutoGenerator<>(CapabilityDefinition::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedApiResource.class, new EntityAutoGenerator<>(RelatedApiResource::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedEventResource.class, new EntityAutoGenerator<>(RelatedEventResource::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedCapability.class, new EntityAutoGenerator<>(RelatedCapability::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(28, Ord.Capability.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new Capability()
            .withType("custom")
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withOrdId(NAMESPACE + ":capability:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.Capability.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    assertEquals(
        new Capability()
            .withType("custom")
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":capability:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.Capability.class),
            getClass(),
            new DocumentSchema()
                .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))))));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Capability annotation = Annotations.mock(
        Ord.Capability.class,
        Map.ofEntries(
            Map.entry("disabled", true),
            Map.entry("version", "2.0.0"),
            Map.entry("type", "test-type"),
            Map.entry("title", "MyCapability"),
            Map.entry("visibility", "internal"),
            Map.entry("minSystemVersion", "1.0.0"),
            Map.entry("systemInstanceAware", true),
            Map.entry("localId", "custom-local-id"),
            Map.entry("releaseStatus", "deprecated"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("description", "Custom description"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("ordId", NAMESPACE + ":capability:My:v1"),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("relatedEntityTypes", new String[] {"entity-type-1", "entity-type-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry(
                "definitions",
                new Ord.CapabilityDefinition[] {createCapabilityDefinitionAnnotationMock()}),
            Map.entry(
                "relatedApiResources",
                new Ord.RelatedApiResource[] {createRelatedApiResourceAnnotationMock()}),
            Map.entry(
                "relatedEventResources",
                new Ord.RelatedEventResource[] {createRelatedEventResourceAnnotationMock()}),
            Map.entry(
                "relatedCapabilities",
                new Ord.RelatedCapability[] {createRelatedCapabilityAnnotationMock()})));

    assertEquals(
        new Capability()
            .withDisabled(true)
            .withVersion("2.0.0")
            .withType("test-type")
            .withTitle("MyCapability")
            .withVisibility("internal")
            .withMinSystemVersion("1.0.0")
            .withSystemInstanceAware(true)
            .withLocalId("custom-local-id")
            .withReleaseStatus("deprecated")
            .withCustomType("test-custom-type")
            .withTags(List.of("tag-1", "tag-2"))
            .withDescription("Custom description")
            .withOrdId(NAMESPACE + ":capability:My:v1")
            .withShortDescription("Custom short description")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withRelatedEntityTypes(List.of("entity-type-1", "entity-type-2"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withLinks(List.of(new Link()
                .withTitle("test-link-title")
                .withDescription("test-link-description")
                .withUrl(URI.create("https://test-link.dummy.nowhere.org"))))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-doc-label-key",
                    List.of("test-doc-label-value-1", "test-doc-label-value-2")))
            .withDefinitions(List.of(new CapabilityDefinition()
                .withType("custom")
                .withVisibility("public")
                .withMediaType("application/json")
                .withCustomType("test-custom-type")
                .withUrl("https://test-definition.dummy.nowhere.org")
                .withAccessStrategies(List.of(new AccessStrategy()
                    .withType("open")
                    .withCustomType("test-access-strategy-custom-type")
                    .withCustomDescription("test-access-strategy-custom-description")))))
            .withRelatedApiResources(List.of(new RelatedApiResource()
                .withOrdId("test-related-api-resource-ord-id")
                .withRelationType("test-relation-type")))
            .withRelatedEventResources(List.of(new RelatedEventResource()
                .withOrdId("test-related-event-resource-ord-id")
                .withRelationType("test-relation-type")))
            .withRelatedCapabilities(List.of(new RelatedCapability()
                .withOrdId("test-related-capability-ord-id")
                .withRelationType("test-relation-type"))),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.Link createLinkAnnotationMock() {
    return Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("description", "test-link-description"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));
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

  private static Ord.DocumentationLabels createDocumentationLabelsAnnotationMock() {
    return Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
      Annotations.mock(
          Ord.DocumentationLabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-doc-label-key"),
              Map.entry("values", new String[] {"test-doc-label-value-1", "test-doc-label-value-2"})))
    }));
  }

  private static Ord.CapabilityDefinition createCapabilityDefinitionAnnotationMock() {
    return Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("visibility", "public"),
            Map.entry("mediaType", "application/json"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org"),
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

  private static Ord.RelatedCapability createRelatedCapabilityAnnotationMock() {
    return Annotations.mock(
        Ord.RelatedCapability.class,
        Map.ofEntries(
            Map.entry("ordId", "test-related-capability-ord-id"),
            Map.entry("relationType", "test-relation-type")));
  }
}
