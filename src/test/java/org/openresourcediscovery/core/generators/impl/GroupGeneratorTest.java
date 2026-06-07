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
import org.openresourcediscovery.model.Group;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class GroupGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private GroupGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new GroupGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(10, Ord.Group.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new Group()
            .withTitle(getClass().getSimpleName())
            .withGroupTypeId(
                NAMESPACE + ":" + getClass().getSimpleName().toLowerCase())
            .withGroupId(NAMESPACE
                + ":"
                + getClass().getSimpleName().toLowerCase()
                + ":"
                + NAMESPACE
                + ":"
                + getClass().getSimpleName()),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.Group.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenCustomGroupTypeId_whenGenerateIsCalled_thenGroupIdUsesCustomGroupTypeId() {
    Ord.Group annotation =
        Annotations.mock(Ord.Group.class, Map.ofEntries(Map.entry("groupTypeId", "custom:grouptype")));

    assertEquals(
        new Group()
            .withGroupTypeId("custom:grouptype")
            .withTitle(getClass().getSimpleName())
            .withGroupId("custom:grouptype:" + NAMESPACE + ":"
                + getClass().getSimpleName()),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Group annotation = Annotations.mock(
        Ord.Group.class,
        Map.ofEntries(
            Map.entry("title", "My Group"),
            Map.entry("visibility", "internal"),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("groupTypeId", NAMESPACE + ":mygroup"),
            Map.entry("description", "My group description"),
            Map.entry("groupId", NAMESPACE + ":mygroup:" + NAMESPACE + ":MyGroup"),
            Map.entry("partOfGroups", new String[] {"parent-group-1", "parent-group-2"}),
            Map.entry("correlationIds", new String[] {"correlation-id-1", "correlation-id-2"})));

    assertEquals(
        new Group()
            .withTitle("My Group")
            .withVisibility("internal")
            .withGroupTypeId(NAMESPACE + ":mygroup")
            .withDescription("My group description")
            .withGroupId(NAMESPACE + ":mygroup:" + NAMESPACE + ":MyGroup")
            .withPartOfGroups(List.of("parent-group-1", "parent-group-2"))
            .withCorrelationIds(List.of("correlation-id-1", "correlation-id-2"))
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
