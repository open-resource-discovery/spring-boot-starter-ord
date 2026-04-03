package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.AccessStrategy;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class AccessStrategyGeneratorTest {

  private EntityAutoGenerator<Ord.AccessStrategy, AccessStrategy> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(AccessStrategy::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.AccessStrategy.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(
            Context.of(Annotations.mock(Ord.AccessStrategy.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.AccessStrategy annotation =
        Annotations.mock(Ord.AccessStrategy.class, Map.ofEntries(Map.entry("type", "open")));

    assertEquals(
        new AccessStrategy().withType("open"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.AccessStrategy annotation = Annotations.mock(
        Ord.AccessStrategy.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("customDescription", "test-custom-description")));

    assertEquals(
        new AccessStrategy()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withCustomDescription("test-custom-description"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
