package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.testutils.Annotations;

class LinkGeneratorTest {

  private EntityAutoGenerator<Ord.Link, Link> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(Link::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.Link.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTitleIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class, Map.ofEntries(Map.entry("url", "https://test-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.Link annotation = Annotations.mock(Ord.Link.class, Map.ofEntries(Map.entry("title", "test-link-title")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));

    assertEquals(
        new Link().withTitle("test-link-title").withUrl(URI.create("https://test-link.dummy.nowhere.org")),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("description", "test-link-description"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));

    assertEquals(
        new Link()
            .withTitle("test-link-title")
            .withDescription("test-link-description")
            .withUrl(URI.create("https://test-link.dummy.nowhere.org")),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
