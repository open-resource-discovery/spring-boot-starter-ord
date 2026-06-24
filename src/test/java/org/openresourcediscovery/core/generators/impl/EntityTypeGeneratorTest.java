package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.EntityType;
import org.openresourcediscovery.model.EntityTypeDefinition;
import org.openresourcediscovery.model.Extensible;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.RelatedEntityType;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class EntityTypeGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.EntityType, EntityType> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityTypeGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityTypeGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Extensible.class, new ExtensibleGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new) {});
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new) {});
    prepareEntityGeneratorFactoryMock(Ord.ChangelogEntry.class, new EntityAutoGenerator<>(ChangelogEntry::new) {});
    prepareEntityGeneratorFactoryMock(
        Ord.EntityTypeDefinition.class, new EntityAutoGenerator<>(EntityTypeDefinition::new) {});
    prepareEntityGeneratorFactoryMock(
        Ord.RelatedEntityType.class, new EntityAutoGenerator<>(RelatedEntityType::new) {});
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(32, Ord.EntityType.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.EntityType> context =
        Context.of(Annotations.mock(Ord.EntityType.class), getClass(), new DocumentSchema());

    assertEquals(
        new EntityType()
            .withVersion("1.0.0")
            .withLevel("aggregate")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withLocalId(getClass().getSimpleName().toLowerCase())
            .withOrdId(NAMESPACE + ":entityType:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    Context<Ord.EntityType> context = Context.of(
        Annotations.mock(Ord.EntityType.class),
        getClass(),
        new DocumentSchema()
            .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))));

    assertEquals(
        new EntityType()
            .withVersion("1.0.0")
            .withLevel("aggregate")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withLocalId(getClass().getSimpleName().toLowerCase())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":entityType:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EntityType annotation = Annotations.mock(
        Ord.EntityType.class,
        Map.ofEntries(
            Map.entry("aiHint", "aiHint"),
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("releaseStatus", "beta"),
            Map.entry("level", "sub-aggregate"),
            Map.entry("visibility", "internal"),
            Map.entry("systemInstanceAware", true),
            Map.entry("localId", "custom-local-id"),
            Map.entry("policyLevel", "test-policy-level"),
            Map.entry("description", "Custom description"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("sunsetDate", "2027-03-25T14:30:00Z"),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("deprecationDate", "2026-03-25T14:30:00Z"),
            Map.entry("extensible", createExtensibleAnnotationMock()),
            Map.entry("ordId", NAMESPACE + ":entityType:Custom:v1"),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("customPolicyLevel", "test-custom-policy-level"),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("successors", new String[] {"successor-1", "successor-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("partOfProducts", new String[] {"test-product-1", "test-product-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("policyLevels", new String[] {"test-policy-level-1", "test-policy-level-2"}),
            Map.entry("changelogEntries", new Ord.ChangelogEntry[] {createChangelogEntryAnnotationMock()}),
            Map.entry(
                "definitions",
                new Ord.EntityTypeDefinition[] {createEntityTypeDefinitionAnnotationMock()}),
            Map.entry(
                "relatedEntityTypes",
                new Ord.RelatedEntityType[] {createRelatedEntityTypeAnnotationMock()})));
    Context<Ord.EntityType> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EntityType()
            .withAiHint("aiHint")
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withReleaseStatus("beta")
            .withLevel("sub-aggregate")
            .withVisibility("internal")
            .withSystemInstanceAware(true)
            .withLocalId("custom-local-id")
            .withTags(List.of("tag-1", "tag-2"))
            .withDescription("Custom description")
            .withPolicyLevel("test-policy-level")
            .withOrdId(NAMESPACE + ":entityType:Custom:v1")
            .withShortDescription("Custom short description")
            .withCustomPolicyLevel("test-custom-policy-level")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withSuccessors(List.of("successor-1", "successor-2"))
            .withPartOfProducts(List.of("test-product-1", "test-product-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withSunsetDate(Commons.asDate("2027-03-25T14:30:00Z"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withDeprecationDate(Commons.asDate("2026-03-25T14:30:00Z"))
            .withPolicyLevels(List.of("test-policy-level-1", "test-policy-level-2"))
            .withExtensible(new Extensible().withSupported("manual").withDescription("test-description"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withRelatedEntityTypes(List.of(new RelatedEntityType()
                .withRelationType("parent")
                .withOrdId(NAMESPACE + ":entityType:Related:v1")))
            .withLinks(List.of(new Link()
                .withTitle("test-link-title")
                .withDescription("test-link-description")
                .withUrl(URI.create("https://test-link.dummy.nowhere.org"))))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-doc-label-key",
                    List.of("test-doc-label-value-1", "test-doc-label-value-2")))
            .withChangelogEntries(List.of(new ChangelogEntry()
                .withVersion("1.0.0")
                .withDate("2025-01-01")
                .withReleaseStatus("active")
                .withDescription("test-changelog-description")
                .withUrl(URI.create("https://test-changelog.dummy.nowhere.org"))))
            .withDefinitions(List.of(new EntityTypeDefinition()
                .withType("test-type")
                .withVisibility("internal")
                .withMediaType("application/json")
                .withUrl("http://localhost:8080/ord/v1/entityTypes/definition-1.json")
                .withAccessStrategies(List.of(new AccessStrategy()
                    .withType("open")
                    .withCustomType("test-access-strategy-custom-type")
                    .withCustomDescription("test-access-strategy-custom-description"))))),
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

  private static Ord.AccessStrategy createAccessStrategyAnnotationMock() {
    return Annotations.mock(
        Ord.AccessStrategy.class,
        Map.ofEntries(
            Map.entry("type", "open"),
            Map.entry("customType", "test-access-strategy-custom-type"),
            Map.entry("customDescription", "test-access-strategy-custom-description")));
  }

  private static Ord.ChangelogEntry createChangelogEntryAnnotationMock() {
    return Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(
            Map.entry("version", "1.0.0"),
            Map.entry("releaseStatus", "active"),
            Map.entry("date", "2025-01-01"),
            Map.entry("description", "test-changelog-description"),
            Map.entry("url", "https://test-changelog.dummy.nowhere.org")));
  }

  private static Ord.RelatedEntityType createRelatedEntityTypeAnnotationMock() {
    return Annotations.mock(
        Ord.RelatedEntityType.class,
        Map.ofEntries(
            Map.entry("relationType", "parent"), Map.entry("ordId", NAMESPACE + ":entityType:Related:v1")));
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

  private static Ord.EntityTypeDefinition createEntityTypeDefinitionAnnotationMock() {
    return Annotations.mock(
        Ord.EntityTypeDefinition.class,
        Map.ofEntries(
            Map.entry("type", "test-type"),
            Map.entry("visibility", "internal"),
            Map.entry("mediaType", "application/json"),
            Map.entry("url", "http://localhost:8080/ord/v1/entityTypes/definition-1.json"),
            Map.entry(
                "accessStrategies", new Ord.AccessStrategy[] {createAccessStrategyAnnotationMock()})));
  }
}
