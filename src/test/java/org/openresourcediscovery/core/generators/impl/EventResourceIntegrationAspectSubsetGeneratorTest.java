package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.testutils.Annotations;

class EventResourceIntegrationAspectSubsetGeneratorTest {

  private EntityAutoGenerator<Ord.EventResourceIntegrationAspectSubset, EventResourceIntegrationAspectSubset>
      classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(EventResourceIntegrationAspectSubset::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.EventResourceIntegrationAspectSubset.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatEventTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.EventResourceIntegrationAspectSubset.class),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventResourceIntegrationAspectSubset annotation = Annotations.mock(
        Ord.EventResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("eventType", "test-event-type")));

    assertEquals(
        new EventResourceIntegrationAspectSubset().withEventType("test-event-type"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
