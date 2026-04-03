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
import org.openresourcediscovery.model.GroupType;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class GroupTypeGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private GroupTypeGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new GroupTypeGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(8, Ord.GroupType.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new GroupType()
            .withTitle(getClass().getSimpleName())
            .withGroupTypeId(
                NAMESPACE + ":" + getClass().getSimpleName().toLowerCase()),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.GroupType.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.GroupType annotation = Annotations.mock(
        Ord.GroupType.class,
        Map.ofEntries(
            Map.entry("title", "My Group Type"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("groupTypeId", NAMESPACE + ":mygrouptype"),
            Map.entry("description", "My group type description"),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"}),
            Map.entry("partOfGroupTypes", new String[] {"parent-group-type-1", "parent-group-type-2"})));

    assertEquals(
        new GroupType()
            .withTitle("My Group Type")
            .withGroupTypeId(NAMESPACE + ":mygrouptype")
            .withDescription("My group type description")
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
            .withPartOfGroupTypes(List.of("parent-group-type-1", "parent-group-type-2"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2"))),
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
}
