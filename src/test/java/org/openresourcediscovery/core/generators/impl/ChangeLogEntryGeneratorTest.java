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
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class ChangeLogEntryGeneratorTest {

  private EntityAutoGenerator<Ord.ChangelogEntry, ChangelogEntry> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ChangelogEntry::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(6, Ord.ChangelogEntry.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatDateIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ChangelogEntry annotation = Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(Map.entry("version", "1.0.0"), Map.entry("releaseStatus", "active")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatVersionIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ChangelogEntry annotation = Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(Map.entry("releaseStatus", "active"), Map.entry("date", "2025-03-25T14:30:00Z")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatReleaseStatusIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ChangelogEntry annotation = Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(Map.entry("version", "1.0.0"), Map.entry("date", "2025-03-25T14:30:00Z")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ChangelogEntry annotation = Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(
            Map.entry("version", "1.0.0"),
            Map.entry("releaseStatus", "active"),
            Map.entry("date", "2025-03-25T14:30:00Z")));

    assertEquals(
        new ChangelogEntry()
            .withVersion("1.0.0")
            .withReleaseStatus("active")
            .withDate("2025-03-25T14:30:00Z"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ChangelogEntry annotation = Annotations.mock(
        Ord.ChangelogEntry.class,
        Map.ofEntries(
            Map.entry("version", "1.0.0"),
            Map.entry("releaseStatus", "active"),
            Map.entry("date", "2025-03-25T14:30:00Z"),
            Map.entry("description", "test-description"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));

    assertEquals(
        new ChangelogEntry()
            .withVersion("1.0.0")
            .withReleaseStatus("active")
            .withDate("2025-03-25T14:30:00Z")
            .withDescription("test-description")
            .withUrl(URI.create("https://test-link.dummy.nowhere.org")),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
