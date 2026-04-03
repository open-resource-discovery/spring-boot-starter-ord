package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DataProductLink;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class DataProductLinkGeneratorTest {

  private EntityAutoGenerator<Ord.DataProductLink, DataProductLink> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(DataProductLink::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.DataProductLink.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.DataProductLink annotation =
        Annotations.mock(Ord.DataProductLink.class, Map.ofEntries(Map.entry("type", "custom")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(
            Map.entry("type", "api-documentation"),
            Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));

    assertEquals(
        new DataProductLink()
            .withType("api-documentation")
            .withUrl("https://test-data-product-link.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));

    assertEquals(
        new DataProductLink()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withUrl("https://test-data-product-link.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
