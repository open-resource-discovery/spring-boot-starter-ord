package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.SystemVersion;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class SystemVersionGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.SystemVersion, SystemVersion> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private SystemVersionGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new SystemVersionGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(8, Ord.SystemVersion.class.getDeclaredMethods().length);
  }

  @Test
  void givenEmptyAnnotation_whenGenerateIsCalled_thenNullIsReturned() {
    assertNull(classUnderTest.generate(EntityGenerator.Context.of(
        Annotations.mock(Ord.SystemVersion.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectValueIsReturned() {
    Ord.SystemVersion annotation = Annotations.mock(
        Ord.SystemVersion.class,
        Map.ofEntries(
            Map.entry("version", "2.0.0"),
            Map.entry("title", "My Version"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"test-tag-1", "test-tag-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("correlationIds", new String[] {"test-correlation-id-1", "test-correlation-id-2"})));
    Context<Ord.SystemVersion> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new SystemVersion()
            .withVersion("2.0.0")
            .withTitle("My Version")
            .withTags(List.of("test-tag-1", "test-tag-2"))
            .withCorrelationIds(List.of("test-correlation-id-1", "test-correlation-id-2"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-document-label-key",
                    List.of("test-document-label-value-1", "test-document-label-value-2"))),
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
