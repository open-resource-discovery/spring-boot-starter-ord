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
import org.openresourcediscovery.model.APIEventResourceLink;
import org.openresourcediscovery.model.AccessStrategy;
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.ConsumptionBundleReference;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.EntityTypeMapping;
import org.openresourcediscovery.model.EventCompatibility;
import org.openresourcediscovery.model.EventResource;
import org.openresourcediscovery.model.EventResourceDefinition;
import org.openresourcediscovery.model.ExposedEntityType;
import org.openresourcediscovery.model.Extensible;
import org.openresourcediscovery.model.Extensible.Supported;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.RelatedApiResource;
import org.openresourcediscovery.model.RelatedEventResource;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class EventResourceGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EventResourceGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EventResourceGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Extensible.class, new ExtensibleGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new));
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.ApiModelSelectorOData.class, new ApiModelSelectorODataGenerator());
    prepareEntityGeneratorFactoryMock(Ord.EntityTypeOrdIdTarget.class, new EntityTypeOrdIdTargetGenerator());
    prepareEntityGeneratorFactoryMock(Ord.ChangelogEntry.class, new EntityAutoGenerator<>(ChangelogEntry::new));
    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new));
    prepareEntityGeneratorFactoryMock(
        Ord.EntityTypeMapping.class, new EntityAutoGenerator<>(EntityTypeMapping::new));
    prepareEntityGeneratorFactoryMock(
        Ord.ExposedEntityType.class, new EntityAutoGenerator<>(ExposedEntityType::new));
    prepareEntityGeneratorFactoryMock(
        Ord.EventCompatibility.class, new EntityAutoGenerator<>(EventCompatibility::new));
    prepareEntityGeneratorFactoryMock(
        Ord.APIEventResourceLink.class, new EntityAutoGenerator<>(APIEventResourceLink::new));
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceDefinition.class, new EntityAutoGenerator<>(EventResourceDefinition::new));
    prepareEntityGeneratorFactoryMock(
        Ord.ConsumptionBundleReference.class, new EntityAutoGenerator<>(ConsumptionBundleReference::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedApiResource.class, new EntityAutoGenerator<>(RelatedApiResource::new));
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedEventResource.class, new EntityAutoGenerator<>(RelatedEventResource::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(47, Ord.EventResource.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new EventResource()
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withOrdId(NAMESPACE + ":eventResource:" + getClass().getSimpleName() + ":v1")
            .withDescription(
                "Auto-generated description for " + getClass().getSimpleName())
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName()),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.EventResource.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    assertEquals(
        new EventResource()
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":eventResource:" + getClass().getSimpleName() + ":v1")
            .withDescription(
                "Auto-generated description for " + getClass().getSimpleName())
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName()),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.EventResource.class),
            getClass(),
            new DocumentSchema()
                .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))))));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventResource annotation = Annotations.mock(
        Ord.EventResource.class,
        Map.ofEntries(
            Map.entry("disabled", true),
            Map.entry("_abstract", true),
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("releaseStatus", "beta"),
            Map.entry("visibility", "internal"),
            Map.entry("minSystemVersion", "1.0.0"),
            Map.entry("systemInstanceAware", true),
            Map.entry("localId", "custom-local-id"),
            Map.entry("responsible", "test-responsible"),
            Map.entry("policyLevel", "test-policy-level"),
            Map.entry("description", "Custom description"),
            Map.entry("countries", new String[] {"DE", "US"}),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("sunsetDate", "2027-03-25T14:30:00Z"),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("deprecationDate", "2026-03-25T14:30:00Z"),
            Map.entry("extensible", createExtensibleAnnotationMock()),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("ordId", NAMESPACE + ":eventResource:Custom:v1"),
            Map.entry("customPolicyLevel", "test-custom-policy-level"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("successors", new String[] {"successor-1", "successor-2"}),
            Map.entry("implementationStandard", "test-implementation-standard"),
            Map.entry("lineOfBusiness", new String[] {"test-lob-1", "test-lob-2"}),
            Map.entry("customImplementationStandard", "test-custom-impl-standard"),
            Map.entry("industry", new String[] {"test-industry-1", "test-industry-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("partOfProducts", new String[] {"test-product-1", "test-product-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("defaultConsumptionBundle", NAMESPACE + ":consumptionBundle:default:v1"),
            Map.entry("policyLevels", new String[] {"test-policy-level-1", "test-policy-level-2"}),
            Map.entry("customImplementationStandardDescription", "test-custom-impl-standard-desc"),
            Map.entry("changelogEntries", new Ord.ChangelogEntry[] {createChangelogEntryAnnotationMock()}),
            Map.entry(
                "compatibleWith",
                new Ord.EventCompatibility[] {createEventCompatibilityAnnotationMock()}),
            Map.entry(
                "entityTypeMappings",
                new Ord.EntityTypeMapping[] {createEntityTypeMappingAnnotationMock()}),
            Map.entry(
                "exposedEntityTypes",
                new Ord.ExposedEntityType[] {createExposedEntityTypeAnnotationMock()}),
            Map.entry(
                "eventResourceLinks",
                new Ord.APIEventResourceLink[] {createAPIEventResourceLinkAnnotationMock()}),
            Map.entry(
                "resourceDefinitions",
                new Ord.EventResourceDefinition[] {createEventResourceDefinitionAnnotationMock()}),
            Map.entry("partOfConsumptionBundles", new Ord.ConsumptionBundleReference[] {
              createConsumptionBundleReferenceAnnotationMock()
            }),
            Map.entry(
                "relatedApiResources",
                new Ord.RelatedApiResource[] {createRelatedApiResourceAnnotationMock()}),
            Map.entry(
                "relatedEventResources",
                new Ord.RelatedEventResource[] {createRelatedEventResourceAnnotationMock()})));

    assertEquals(
        new EventResource()
            .withDisabled(true)
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withReleaseStatus("beta")
            .withVisibility("internal")
            .withAbstract(true)
            .withSystemInstanceAware(true)
            .withMinSystemVersion("1.0.0")
            .withLocalId("custom-local-id")
            .withCountries(List.of("DE", "US"))
            .withResponsible("test-responsible")
            .withTags(List.of("tag-1", "tag-2"))
            .withPolicyLevel("test-policy-level")
            .withDescription("Custom description")
            .withShortDescription("Custom short description")
            .withOrdId(NAMESPACE + ":eventResource:Custom:v1")
            .withCustomPolicyLevel("test-custom-policy-level")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withSuccessors(List.of("successor-1", "successor-2"))
            .withLineOfBusiness(List.of("test-lob-1", "test-lob-2"))
            .withImplementationStandard("test-implementation-standard")
            .withIndustry(List.of("test-industry-1", "test-industry-2"))
            .withCustomImplementationStandard("test-custom-impl-standard")
            .withPartOfProducts(List.of("test-product-1", "test-product-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withSunsetDate(Commons.asDate("2027-03-25T14:30:00Z"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withDeprecationDate(Commons.asDate("2026-03-25T14:30:00Z"))
            .withPolicyLevels(List.of("test-policy-level-1", "test-policy-level-2"))
            .withDefaultConsumptionBundle(NAMESPACE + ":consumptionBundle:default:v1")
            .withCustomImplementationStandardDescription("test-custom-impl-standard-desc")
            .withExtensible(
                new Extensible().withSupported(Supported.MANUAL).withDescription("test-description"))
            .withExposedEntityTypes(
                List.of(new ExposedEntityType().withOrdId(NAMESPACE + ":entityType:TestEntity:v1")))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withCompatibleWith(List.of(new EventCompatibility()
                .withMaxVersion("1.0.0")
                .withOrdId(NAMESPACE + ":eventResource:Compatible:v1")))
            .withLinks(List.of(new Link()
                .withTitle("test-link-title")
                .withDescription("test-link-description")
                .withUrl(URI.create("https://test-link.dummy.nowhere.org"))))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-doc-label-key",
                    List.of("test-doc-label-value-1", "test-doc-label-value-2")))
            .withPartOfConsumptionBundles(List.of(new ConsumptionBundleReference()
                .withOrdId(NAMESPACE + ":consumptionBundle:test:v1")
                .withDefaultEntryPoint("https://test-entry-point.dummy.nowhere.org")))
            .withEventResourceLinks(List.of(new APIEventResourceLink()
                .withType("test-event-resource-link-type")
                .withCustomType("test-event-resource-link-custom-type")
                .withUrl("https://test-event-resource-link.dummy.nowhere.org")))
            .withChangelogEntries(List.of(new ChangelogEntry()
                .withVersion("1.0.0")
                .withDate("2025-01-01")
                .withReleaseStatus("active")
                .withDescription("test-changelog-description")
                .withUrl(URI.create("https://test-changelog.dummy.nowhere.org"))))
            .withEntityTypeMappings(List.of(new EntityTypeMapping()
                .withApiModelSelectors(null)
                .withEntityTypeTargets(
                    List.of(Map.of("ordId", NAMESPACE + ":entityType:TestEntity:v1")))
                .withApiModelSelectors(
                    List.of(Map.of("type", "test-type", "entitySetName", "test-entity-set-name")))))
            .withResourceDefinitions(List.of(new EventResourceDefinition()
                .withType("asyncapi-v2")
                .withVisibility("public")
                .withMediaType("application/json")
                .withCustomType("test-custom-type")
                .withUrl("https://test-resource-definition.dummy.nowhere.org")
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

  private static Ord.Extensible createExtensibleAnnotationMock() {
    return Annotations.mock(
        Ord.Extensible.class,
        Map.ofEntries(Map.entry("supported", "manual"), Map.entry("description", "test-description")));
  }

  private static Ord.ChangelogEntry createChangelogEntryAnnotationMock() {
    return Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(
            Map.entry("version", "1.0.0"),
            Map.entry("date", "2025-01-01"),
            Map.entry("releaseStatus", "active"),
            Map.entry("description", "test-changelog-description"),
            Map.entry("url", "https://test-changelog.dummy.nowhere.org")));
  }

  private static Ord.AccessStrategy createAccessStrategyAnnotationMock() {
    return Annotations.mock(
        Ord.AccessStrategy.class,
        Map.ofEntries(
            Map.entry("type", "open"),
            Map.entry("customType", "test-access-strategy-custom-type"),
            Map.entry("customDescription", "test-access-strategy-custom-description")));
  }

  private static Ord.EntityTypeMapping createEntityTypeMappingAnnotationMock() {
    return Annotations.mock(
        Ord.EntityTypeMapping.class,
        Map.ofEntries(
            Map.entry("apiModelSelectors", new Ord.ApiModelSelectorOData[] {
              Annotations.mock(
                  Ord.ApiModelSelectorOData.class,
                  Map.ofEntries(
                      Map.entry("type", "test-type"),
                      Map.entry("entitySetName", "test-entity-set-name")))
            }),
            Map.entry("entityTypeTargets", new Ord.EntityTypeOrdIdTarget[] {
              Annotations.mock(
                  Ord.EntityTypeOrdIdTarget.class,
                  Map.ofEntries(Map.entry("ordId", NAMESPACE + ":entityType:TestEntity:v1")))
            })));
  }

  private static Ord.ExposedEntityType createExposedEntityTypeAnnotationMock() {
    return Annotations.mock(
        Ord.ExposedEntityType.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":entityType:TestEntity:v1")));
  }

  private static Ord.EventCompatibility createEventCompatibilityAnnotationMock() {
    return Annotations.mock(
        Ord.EventCompatibility.class,
        Map.ofEntries(
            Map.entry("maxVersion", "1.0.0"),
            Map.entry("ordId", NAMESPACE + ":eventResource:Compatible:v1")));
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

  private static Ord.APIEventResourceLink createAPIEventResourceLinkAnnotationMock() {
    return Annotations.mock(
        Ord.APIEventResourceLink.class,
        Map.ofEntries(
            Map.entry("type", "test-event-resource-link-type"),
            Map.entry("customType", "test-event-resource-link-custom-type"),
            Map.entry("url", "https://test-event-resource-link.dummy.nowhere.org")));
  }

  private static Ord.EventResourceDefinition createEventResourceDefinitionAnnotationMock() {
    return Annotations.mock(
        Ord.EventResourceDefinition.class,
        Map.ofEntries(
            Map.entry("type", "asyncapi-v2"),
            Map.entry("visibility", "public"),
            Map.entry("mediaType", "application/json"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-resource-definition.dummy.nowhere.org"),
            Map.entry(
                "accessStrategies", new Ord.AccessStrategy[] {createAccessStrategyAnnotationMock()})));
  }

  private static Ord.ConsumptionBundleReference createConsumptionBundleReferenceAnnotationMock() {
    return Annotations.mock(
        Ord.ConsumptionBundleReference.class,
        Map.ofEntries(
            Map.entry("ordId", NAMESPACE + ":consumptionBundle:test:v1"),
            Map.entry("defaultEntryPoint", "https://test-entry-point.dummy.nowhere.org")));
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
