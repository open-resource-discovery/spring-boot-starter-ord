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
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Vendor;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class VendorGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private VendorGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new VendorGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(8, Ord.Vendor.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new Vendor()
            .withTitle(getClass().getSimpleName())
            .withOrdId(NAMESPACE + ":vendor:" + getClass().getSimpleName() + ":v1"),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.Vendor.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Vendor annotation = Annotations.mock(
        Ord.Vendor.class,
        Map.ofEntries(
            Map.entry("title", "ACME Inc."),
            Map.entry("ordId", "acme:vendor:ACME:v1"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"test-label-1", "test-label-2"}),
            Map.entry("partners", new String[] {"Road Runner", "Willie E. Coyote"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock())));

    assertEquals(
        new Vendor()
            .withTitle("ACME Inc.")
            .withOrdId("acme:vendor:ACME:v1")
            .withTags(List.of("test-label-1", "test-label-2"))
            .withPartners(List.of("Road Runner", "Willie E. Coyote"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-document-label-key",
                    List.of("test-document-label-value-1", "test-document-label-value-2"))),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
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
