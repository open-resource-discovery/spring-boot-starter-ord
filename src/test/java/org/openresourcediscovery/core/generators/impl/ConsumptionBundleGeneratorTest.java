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
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.CredentialExchangeStrategy;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class ConsumptionBundleGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.ConsumptionBundle, ConsumptionBundle> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private ConsumptionBundleGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ConsumptionBundleGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new) {});
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(
        Ord.CredentialExchangeStrategy.class, new EntityAutoGenerator<>(CredentialExchangeStrategy::new) {});
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(16, Ord.ConsumptionBundle.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    Context<Ord.ConsumptionBundle> context =
        Context.of(Annotations.mock(Ord.ConsumptionBundle.class), getClass(), new DocumentSchema());

    assertEquals(
        new ConsumptionBundle()
            .withTitle(getClass().getSimpleName())
            .withOrdId(
                NAMESPACE + ":consumptionBundle:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ConsumptionBundle annotation = Annotations.mock(
        Ord.ConsumptionBundle.class,
        Map.ofEntries(
            Map.entry("version", "2.0.0"),
            Map.entry("title", "My Bundle"),
            Map.entry("visibility", "internal"),
            Map.entry("localId", "custom-local-id"),
            Map.entry("description", "Custom description"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("lastUpdate", "2025-03-25T14:30:00Z"),
            Map.entry("shortDescription", "Custom short description"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("ordId", NAMESPACE + ":consumptionBundle:Custom:v1"),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("credentialExchangeStrategies", new Ord.CredentialExchangeStrategy[] {
              createCredentialExchangeStrategyAnnotationMock()
            })));
    Context<Ord.ConsumptionBundle> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new ConsumptionBundle()
            .withVersion("2.0.0")
            .withTitle("My Bundle")
            .withVisibility("internal")
            .withLocalId("custom-local-id")
            .withTags(List.of("tag-1", "tag-2"))
            .withDescription("Custom description")
            .withShortDescription("Custom short description")
            .withOrdId(NAMESPACE + ":consumptionBundle:Custom:v1")
            .withLastUpdate(Commons.asDate("2025-03-25T14:30:00Z"))
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
            .withCredentialExchangeStrategies(List.of(new CredentialExchangeStrategy()
                .withType("oauth2")
                .withCustomType("test-custom-type")
                .withCustomDescription("test-custom-description")
                .withCallbackUrl(URI.create("https://test-callback.dummy.nowhere.org")))),
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

  private static Ord.DocumentationLabels createDocumentationLabelsAnnotationMock() {
    return Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
      Annotations.mock(
          Ord.DocumentationLabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-doc-label-key"),
              Map.entry("values", new String[] {"test-doc-label-value-1", "test-doc-label-value-2"})))
    }));
  }

  private static Ord.CredentialExchangeStrategy createCredentialExchangeStrategyAnnotationMock() {
    return Annotations.mock(
        Ord.CredentialExchangeStrategy.class,
        Map.ofEntries(
            Map.entry("type", "oauth2"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("customDescription", "test-custom-description"),
            Map.entry("callbackUrl", "https://test-callback.dummy.nowhere.org")));
  }
}
