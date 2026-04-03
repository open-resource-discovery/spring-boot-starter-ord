package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class ApiResourceIntegrationAspectSubsetGeneratorTest {

  private EntityAutoGenerator<Ord.ApiResourceIntegrationAspectSubset, ApiResourceIntegrationAspectSubset>
      classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ApiResourceIntegrationAspectSubset::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.ApiResourceIntegrationAspectSubset.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOperationIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.ApiResourceIntegrationAspectSubset.class),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceIntegrationAspectSubset annotation = Annotations.mock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("operationId", "test-operation-id")));

    assertEquals(
        new ApiResourceIntegrationAspectSubset().withOperationId("test-operation-id"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
