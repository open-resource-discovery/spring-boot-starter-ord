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
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DataProductInputPort;
import org.openresourcediscovery.model.DataProductLink;
import org.openresourcediscovery.model.DataProductOutputPort;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class DataProductGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";
  private static final String APPLICATION = "my-app";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private DataProductGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new DataProductGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    lenient().doReturn(APPLICATION).when(ordProperties).getApplication();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new));
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.ChangelogEntry.class, new EntityAutoGenerator<>(ChangelogEntry::new));
    prepareEntityGeneratorFactoryMock(Ord.DataProductLink.class, new EntityAutoGenerator<>(DataProductLink::new));
    prepareEntityGeneratorFactoryMock(
        Ord.DataProductInputPort.class, new EntityAutoGenerator<>(DataProductInputPort::new));
    prepareEntityGeneratorFactoryMock(
        Ord.DataProductOutputPort.class, new EntityAutoGenerator<>(DataProductOutputPort::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(41, Ord.DataProduct.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    String expectedApplication = APPLICATION.replaceAll("[^A-Za-z0-9._\\-/]", "");
    String expectedNamespace = NAMESPACE.toLowerCase().replaceAll("[^a-z0-9.]", "");

    assertEquals(
        new DataProduct()
            .withType("primary")
            .withVersion("1.0.0")
            .withCategory("other")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withOrdId(NAMESPACE + ":dataProduct:" + getClass().getSimpleName() + ":v1")
            .withDescription(
                "Auto-generated description for " + getClass().getSimpleName())
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName())
            .withResponsible(expectedNamespace + ":" + expectedApplication + ":"
                + getClass().getSimpleName())
            .withOutputPorts(List.of(new DataProductOutputPort()
                .withOrdId(expectedNamespace
                    + ":apiResource:"
                    + getClass().getSimpleName()
                    + ":v1"))),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.DataProduct.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    String expectedApplication = APPLICATION.replaceAll("[^A-Za-z0-9._\\-/]", "");
    String expectedNamespace = NAMESPACE.toLowerCase().replaceAll("[^a-z0-9.]", "");

    assertEquals(
        new DataProduct()
            .withType("primary")
            .withVersion("1.0.0")
            .withCategory("other")
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":dataProduct:" + getClass().getSimpleName() + ":v1")
            .withDescription(
                "Auto-generated description for " + getClass().getSimpleName())
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName())
            .withResponsible(expectedNamespace + ":" + expectedApplication + ":"
                + getClass().getSimpleName())
            .withOutputPorts(List.of(new DataProductOutputPort()
                .withOrdId(expectedNamespace
                    + ":apiResource:"
                    + getClass().getSimpleName()
                    + ":v1"))),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.DataProduct.class),
            getClass(),
            new DocumentSchema()
                .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))))));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProduct annotation = Annotations.mock(
        Ord.DataProduct.class,
        Map.ofEntries(
            Map.entry("disabled", true),
            Map.entry("_abstract", true),
            Map.entry("type", "derived"),
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("visibility", "internal"),
            Map.entry("minSystemVersion", "1.0.0"),
            Map.entry("systemInstanceAware", true),
            Map.entry("lifecycleStatus", "active"),
            Map.entry("localId", "custom-local-id"),
            Map.entry("category", "business-object"),
            Map.entry("releaseStatus", "deprecated"),
            Map.entry("policyLevel", "test-policy-level"),
            Map.entry("description", "Custom description"),
            Map.entry("countries", new String[] {"DE", "US"}),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("sunsetDate", "2027-03-25T14:30:00Z"),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("shortDescription", "Custom short desc"),
            Map.entry("deprecationDate", "2026-03-25T14:30:00Z"),
            Map.entry("responsible", "customer:responsible:team:"),
            Map.entry("ordId", NAMESPACE + ":dataProduct:Custom:v1"),
            Map.entry("customPolicyLevel", "test-custom-policy-level"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("successors", new String[] {"successor-1", "successor-2"}),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("lineOfBusiness", new String[] {"test-lob-1", "test-lob-2"}),
            Map.entry("entityTypes", new String[] {"entity-type-1", "entity-type-2"}),
            Map.entry("industry", new String[] {"test-industry-1", "test-industry-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("partOfProducts", new String[] {"test-product-1", "test-product-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("policyLevels", new String[] {"test-policy-level-1", "test-policy-level-2"}),
            Map.entry("changelogEntries", new Ord.ChangelogEntry[] {createChangelogEntryAnnotationMock()}),
            Map.entry(
                "dataProductLinks", new Ord.DataProductLink[] {createDataProductLinkAnnotationMock()}),
            Map.entry(
                "inputPorts",
                new Ord.DataProductInputPort[] {createDataProductInputPortAnnotationMock()}),
            Map.entry(
                "outputPorts",
                new Ord.DataProductOutputPort[] {createDataProductOutputPortAnnotationMock()})));

    assertEquals(
        new DataProduct()
            .withDisabled(true)
            .withType("derived")
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withVisibility("internal")
            .withAbstract(true)
            .withMinSystemVersion("1.0.0")
            .withSystemInstanceAware(true)
            .withLifecycleStatus("active")
            .withLocalId("custom-local-id")
            .withCategory("business-object")
            .withReleaseStatus("deprecated")
            .withCountries(List.of("DE", "US"))
            .withTags(List.of("tag-1", "tag-2"))
            .withPolicyLevel("test-policy-level")
            .withDescription("Custom description")
            .withShortDescription("Custom short desc")
            .withResponsible("customer:responsible:team:")
            .withOrdId(NAMESPACE + ":dataProduct:Custom:v1")
            .withCustomPolicyLevel("test-custom-policy-level")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withSuccessors(List.of("successor-1", "successor-2"))
            .withLineOfBusiness(List.of("test-lob-1", "test-lob-2"))
            .withEntityTypes(List.of("entity-type-1", "entity-type-2"))
            .withIndustry(List.of("test-industry-1", "test-industry-2"))
            .withPartOfProducts(List.of("test-product-1", "test-product-2"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withSunsetDate(Commons.asDate("2027-03-25T14:30:00Z"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withDeprecationDate(Commons.asDate("2026-03-25T14:30:00Z"))
            .withPolicyLevels(List.of("test-policy-level-1", "test-policy-level-2"))
            .withOutputPorts(List.of(
                new DataProductOutputPort().withOrdId(NAMESPACE + ":dataProduct:Custom:outputPort:v1")))
            .withInputPorts(List.of(new DataProductInputPort()
                .withOrdId(NAMESPACE + ":integrationDependency:Custom:inputPort:v1")))
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
            .withDataProductLinks(List.of(new DataProductLink()
                .withType("custom")
                .withCustomType("test-custom-type")
                .withUrl("https://test-data-product-link.dummy.nowhere.org")))
            .withChangelogEntries(List.of(new ChangelogEntry()
                .withVersion("1.0.0")
                .withDate("2025-01-01")
                .withReleaseStatus("active")
                .withDescription("test-changelog-description")
                .withUrl(URI.create("https://test-changelog.dummy.nowhere.org")))),
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

  private static Ord.DataProductLink createDataProductLinkAnnotationMock() {
    return Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));
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

  private static Ord.DataProductInputPort createDataProductInputPortAnnotationMock() {
    return Annotations.mock(
        Ord.DataProductInputPort.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":integrationDependency:Custom:inputPort:v1")));
  }

  private static Ord.DataProductOutputPort createDataProductOutputPortAnnotationMock() {
    return Annotations.mock(
        Ord.DataProductOutputPort.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":dataProduct:Custom:outputPort:v1")));
  }
}
