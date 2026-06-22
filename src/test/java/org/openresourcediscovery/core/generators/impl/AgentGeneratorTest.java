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
import org.openresourcediscovery.model.Agent;
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.ExposedApiResourcesTarget;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class AgentGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.Agent, Agent> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private AgentGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new AgentGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new));
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.ChangelogEntry.class, new EntityAutoGenerator<>(ChangelogEntry::new));
    prepareEntityGeneratorFactoryMock(
        Ord.ExposedApiResourcesTarget.class, new EntityAutoGenerator<>(ExposedApiResourcesTarget::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(34, Ord.Agent.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.Agent> context = Context.of(Annotations.mock(Ord.Agent.class), getClass(), new DocumentSchema());

    assertEquals(
        new Agent()
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withOrdId(NAMESPACE + ":agent:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    Context<Ord.Agent> context = Context.of(
        Annotations.mock(Ord.Agent.class),
        getClass(),
        new DocumentSchema()
            .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))));

    assertEquals(
        new Agent()
            .withVersion("1.0.0")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":agent:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Agent annotation = Annotations.mock(
        Ord.Agent.class,
        Map.ofEntries(
            Map.entry("disabled", true),
            Map.entry("aiHint", "aiHint"),
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("visibility", "private"),
            Map.entry("releaseStatus", "beta"),
            Map.entry("minSystemVersion", "1.0.0"),
            Map.entry("localId", "custom-local-id"),
            Map.entry("responsible", "test-responsible"),
            Map.entry("description", "Custom description"),
            Map.entry("countries", new String[] {"DE", "US"}),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("sunsetDate", "2027-03-25T14:30:00Z"),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("ordId", NAMESPACE + ":agent:Custom:v1"),
            Map.entry("deprecationDate", "2026-03-25T14:30:00Z"),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("successors", new String[] {"successor-1", "successor-2"}),
            Map.entry("lineOfBusiness", new String[] {"test-lob-1", "test-lob-2"}),
            Map.entry("industry", new String[] {"test-industry-1", "test-industry-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("partOfProducts", new String[] {"test-product-1", "test-product-2"}),
            Map.entry("relatedEntityTypes", new String[] {"entity-type-1", "entity-type-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("policyLevels", new String[] {"test-policy-level-1", "test-policy-level-2"}),
            Map.entry("changelogEntries", new Ord.ChangelogEntry[] {createChangelogEntryAnnotationMock()}),
            Map.entry(
                "integrationDependencies", new String[] {NAMESPACE + ":integrationDependency:Test:v1"}),
            Map.entry("exposedApiResources", new Ord.ExposedApiResourcesTarget[] {
              createExposedApiResourcesTargetAnnotationMock()
            })));

    Context<Ord.Agent> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Agent()
            .withDisabled(true)
            .withAiHint("aiHint")
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withVisibility("private")
            .withReleaseStatus("beta")
            .withMinSystemVersion("1.0.0")
            .withLocalId("custom-local-id")
            .withCountries(List.of("DE", "US"))
            .withTags(List.of("tag-1", "tag-2"))
            .withResponsible("test-responsible")
            .withDescription("Custom description")
            .withOrdId(NAMESPACE + ":agent:Custom:v1")
            .withShortDescription("Custom short description")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withSuccessors(List.of("successor-1", "successor-2"))
            .withLineOfBusiness(List.of("test-lob-1", "test-lob-2"))
            .withIndustry(List.of("test-industry-1", "test-industry-2"))
            .withPartOfProducts(List.of("test-product-1", "test-product-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withSunsetDate(Commons.asDate("2027-03-25T14:30:00Z"))
            .withRelatedEntityTypes(List.of("entity-type-1", "entity-type-2"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withDeprecationDate(Commons.asDate("2026-03-25T14:30:00Z"))
            .withPolicyLevels(List.of("test-policy-level-1", "test-policy-level-2"))
            .withIntegrationDependencies(List.of(NAMESPACE + ":integrationDependency:Test:v1"))
            .withExposedApiResources(List.of(
                new ExposedApiResourcesTarget().withOrdId(NAMESPACE + ":apiResource:TestApi:v1")))
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
            .withChangelogEntries(List.of(new ChangelogEntry()
                .withVersion("1.0.0")
                .withDate("2025-01-01")
                .withReleaseStatus("active")
                .withDescription("test-changelog-description")
                .withUrl(URI.create("https://test-changelog.dummy.nowhere.org")))),
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

  private static Ord.DocumentationLabels createDocumentationLabelsAnnotationMock() {
    return Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
      Annotations.mock(
          Ord.DocumentationLabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-doc-label-key"),
              Map.entry("values", new String[] {"test-doc-label-value-1", "test-doc-label-value-2"})))
    }));
  }

  private static Ord.ExposedApiResourcesTarget createExposedApiResourcesTargetAnnotationMock() {
    return Annotations.mock(
        Ord.ExposedApiResourcesTarget.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":apiResource:TestApi:v1")));
  }
}
