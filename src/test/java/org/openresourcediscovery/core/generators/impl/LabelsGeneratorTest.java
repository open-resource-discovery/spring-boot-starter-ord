package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;

class LabelsGeneratorTest {

  private LabelsGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new LabelsGenerator();
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.Labels.class.getDeclaredMethods().length);
  }

  @Test
  void givenEmptyAnnotationValues_whenGenerateIsCalled_thenNullIsReturned() {
    assertNull(classUnderTest.generate(Context.of(mock(Ord.Labels.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Labels annotation = Annotations.mock(
        Ord.Labels.class, Map.of("value", new Ord.LabelsEntry[] {createLabelsEntryAnnotationMock()}));

    assertEquals(
        new Labels()
            .withAdditionalProperty("test-label-key", List.of("test-label-value-1", "test-label-value-2")),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private static Ord.LabelsEntry createLabelsEntryAnnotationMock() {
    return Annotations.mock(
        Ord.LabelsEntry.class,
        Map.ofEntries(
            Map.entry("key", "test-label-key"),
            Map.entry("values", new String[] {"test-label-value-1", "test-label-value-2"})));
  }
}
