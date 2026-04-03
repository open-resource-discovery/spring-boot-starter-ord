package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

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
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Product;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class ProductGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private ProductGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ProductGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(12, Ord.Product.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new Product()
            .withTitle(getClass().getSimpleName())
            .withVendor("customer:vendor:Customer:")
            .withOrdId(NAMESPACE + ":product:" + getClass().getSimpleName() + ":")
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName()),
        classUnderTest.generate(EntityGenerator.Context.of(
            Annotations.mock(Ord.Product.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Product annotation = Annotations.mock(
        Ord.Product.class,
        Map.ofEntries(
            Map.entry("title", "test-title"),
            Map.entry("ordId", "test-ord-id"),
            Map.entry("vendor", "test-vendor"),
            Map.entry("parent", "test-parent"),
            Map.entry("description", "test-description"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("shortDescription", "test-short-description"),
            Map.entry("tags", new String[] {"test-tag-1", "test-tag-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("correlationIds", new String[] {"test-correlation-id-1", "test-correlation-id-2"})));

    assertEquals(
        new Product()
            .withTitle("test-title")
            .withOrdId("test-ord-id")
            .withVendor("test-vendor")
            .withParent("test-parent")
            .withDescription("test-description")
            .withShortDescription("test-short-description")
            .withTags(List.of("test-tag-1", "test-tag-2"))
            .withCorrelationIds(List.of("test-correlation-id-1", "test-correlation-id-2"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-document-label-key",
                    List.of("test-document-label-value-1", "test-document-label-value-2"))),
        classUnderTest.generate(EntityGenerator.Context.of(annotation, getClass(), new DocumentSchema())));
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

  private static Ord.DocumentationLabels createDocumentationLabelsAnnotationMock() {
    return Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
      Annotations.mock(
          Ord.DocumentationLabelsEntry.class,
          Map.ofEntries(Map.entry("key", "test-document-label-key"), Map.entry("values", new String[] {
            "test-document-label-value-1", "test-document-label-value-2"
          })))
    }));
  }
}
