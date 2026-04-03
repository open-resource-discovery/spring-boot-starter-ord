package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.ApiCompatibility;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class ApiCompatibilityGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.ApiCompatibility, ApiCompatibility> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ApiCompatibility::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.ApiCompatibility.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ApiCompatibility annotation =
        Annotations.mock(Ord.ApiCompatibility.class, Map.ofEntries(Map.entry("maxVersion", "1.0")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatMaxVersionIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ApiCompatibility annotation = Annotations.mock(
        Ord.ApiCompatibility.class, Map.ofEntries(Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiCompatibility annotation = Annotations.mock(
        Ord.ApiCompatibility.class,
        Map.ofEntries(Map.entry("maxVersion", "1.0"), Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1")));

    assertEquals(
        new ApiCompatibility().withMaxVersion("1.0").withOrdId(NAMESPACE + ":apiResource:Test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
