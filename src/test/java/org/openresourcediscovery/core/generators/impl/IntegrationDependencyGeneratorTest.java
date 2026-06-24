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
import org.openresourcediscovery.model.ApiResourceIntegrationAspect;
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.EventResourceIntegrationAspect;
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.IntegrationAspect;
import org.openresourcediscovery.model.IntegrationDependency;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class IntegrationDependencyGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.IntegrationDependency, IntegrationDependency> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private IntegrationDependencyGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new IntegrationDependencyGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new) {});
    prepareEntityGeneratorFactoryMock(Ord.IntegrationAspect.class, new IntegrationAspectGenerator());
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(
        Ord.ApiResourceIntegrationAspect.class,
        new EntityAutoGenerator<>(ApiResourceIntegrationAspect::new) {});
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceIntegrationAspect.class,
        new EntityAutoGenerator<>(EventResourceIntegrationAspect::new) {});
    prepareEntityGeneratorFactoryMock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(ApiResourceIntegrationAspectSubset::new) {});
    prepareEntityGeneratorFactoryMock(
        Ord.EventResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(EventResourceIntegrationAspectSubset::new) {});
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(23, Ord.IntegrationDependency.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.IntegrationDependency> context =
        Context.of(Annotations.mock(Ord.IntegrationDependency.class), getClass(), new DocumentSchema());

    assertEquals(
        new IntegrationDependency()
            .withVersion("1.0.0")
            .withMandatory(false)
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:default:v1")
            .withOrdId(NAMESPACE + ":integrationDependency:"
                + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenSinglePackageInDocument_whenGenerateIsCalled_thenPackageOrdIdIsUsed() {
    Context<Ord.IntegrationDependency> context = Context.of(
        Annotations.mock(Ord.IntegrationDependency.class),
        getClass(),
        new DocumentSchema()
            .withPackages(List.of(new Package().withOrdId(NAMESPACE + ":package:myPackage:v1"))));

    assertEquals(
        new IntegrationDependency()
            .withVersion("1.0.0")
            .withMandatory(false)
            .withVisibility("public")
            .withReleaseStatus("active")
            .withTitle(getClass().getSimpleName())
            .withPartOfPackage(NAMESPACE + ":package:myPackage:v1")
            .withOrdId(NAMESPACE + ":integrationDependency:"
                + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.IntegrationDependency annotation = Annotations.mock(
        Ord.IntegrationDependency.class,
        Map.ofEntries(
            Map.entry("mandatory", true),
            Map.entry("version", "2.0.0"),
            Map.entry("title", "CustomTitle"),
            Map.entry("releaseStatus", "beta"),
            Map.entry("visibility", "internal"),
            Map.entry("localId", "custom-local-id"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("description", "Custom description"),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("sunsetDate", "2027-03-25T14:30:00Z"),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("partOfPackage", NAMESPACE + ":package:custom:v1"),
            Map.entry("successors", new String[] {"successor-1", "successor-2"}),
            Map.entry("partOfGroups", new String[] {"group-id-1", "group-id-2"}),
            Map.entry("ordId", NAMESPACE + ":integrationDependency:Custom:v1"),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("aspects", new Ord.IntegrationAspect[] {createAspectAnnotationMock()}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("relatedIntegrationDependencies", new String[] {"related-dep-1", "related-dep-2"})));

    Context<Ord.IntegrationDependency> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new IntegrationDependency()
            .withMandatory(true)
            .withVersion("2.0.0")
            .withTitle("CustomTitle")
            .withReleaseStatus("beta")
            .withVisibility("internal")
            .withLocalId("custom-local-id")
            .withTags(List.of("tag-1", "tag-2"))
            .withDescription("Custom description")
            .withShortDescription("Custom short description")
            .withPartOfPackage(NAMESPACE + ":package:custom:v1")
            .withSuccessors(List.of("successor-1", "successor-2"))
            .withPartOfGroups(List.of("group-id-1", "group-id-2"))
            .withOrdId(NAMESPACE + ":integrationDependency:Custom:v1")
            .withSunsetDate(Commons.asDate("2027-03-25T14:30:00Z"))
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withRelatedIntegrationDependencies(List.of("related-dep-1", "related-dep-2"))
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
            .withAspects(List.of(new IntegrationAspect()
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
                    .withSystemTypeRestriction(List.of(
                        "test-system-type-restriction-1", "test-system-type-restriction-2"))
                    .withSubset(List.of(new EventResourceIntegrationAspectSubset()
                        .withEventType("test-event-type"))))))),
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

  private static Ord.IntegrationAspect createAspectAnnotationMock() {
    return Annotations.mock(
        Ord.IntegrationAspect.class,
        Map.ofEntries(
            Map.entry("mandatory", true),
            Map.entry("title", "test-aspect-title"),
            Map.entry("supportMultipleProviders", true),
            Map.entry("description", "test-aspect-description"),
            Map.entry("apiResources", new Ord.ApiResourceIntegrationAspect[] {
              createApiResourceIntegrationAspectAnnotationMock()
            }),
            Map.entry("eventResources", new Ord.EventResourceIntegrationAspect[] {
              createEventResourceIntegrationAspectAnnotationMock()
            })));
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
}
