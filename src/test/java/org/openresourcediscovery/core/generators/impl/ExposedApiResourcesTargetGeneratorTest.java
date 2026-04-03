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
import org.openresourcediscovery.model.ExposedApiResourcesTarget;
import org.openresourcediscovery.testutils.Annotations;

class ExposedApiResourcesTargetGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.ExposedApiResourcesTarget, ExposedApiResourcesTarget> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ExposedApiResourcesTarget::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.ExposedApiResourcesTarget.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.ExposedApiResourcesTarget.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ExposedApiResourcesTarget annotation = Annotations.mock(
        Ord.ExposedApiResourcesTarget.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1")));

    assertEquals(
        new ExposedApiResourcesTarget().withOrdId(NAMESPACE + ":apiResource:Test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
