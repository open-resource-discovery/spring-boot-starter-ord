package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.APIEventResourceLink;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class APIEventResourceLinkGeneratorTest {

  private EntityAutoGenerator<Ord.APIEventResourceLink, APIEventResourceLink> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(APIEventResourceLink::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.APIEventResourceLink.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.APIEventResourceLink annotation = Annotations.mock(
        Ord.APIEventResourceLink.class,
        Map.ofEntries(Map.entry("url", "https://test-api-event-resource-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.APIEventResourceLink annotation =
        Annotations.mock(Ord.APIEventResourceLink.class, Map.ofEntries(Map.entry("type", "api-documentation")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.APIEventResourceLink annotation = Annotations.mock(
        Ord.APIEventResourceLink.class,
        Map.ofEntries(
            Map.entry("type", "api-documentation"),
            Map.entry("url", "https://test-api-event-resource-link.dummy.nowhere.org")));

    assertEquals(
        new APIEventResourceLink()
            .withType("api-documentation")
            .withUrl("https://test-api-event-resource-link.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.APIEventResourceLink annotation = Annotations.mock(
        Ord.APIEventResourceLink.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-api-event-resource-link.dummy.nowhere.org")));

    assertEquals(
        new APIEventResourceLink()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withUrl("https://test-api-event-resource-link.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
