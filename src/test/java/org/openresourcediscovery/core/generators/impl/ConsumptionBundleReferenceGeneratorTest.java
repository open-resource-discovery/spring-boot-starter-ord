package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.ConsumptionBundleReference;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class ConsumptionBundleReferenceGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.ConsumptionBundleReference, ConsumptionBundleReference> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ConsumptionBundleReference::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.ConsumptionBundleReference.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ConsumptionBundleReference annotation = Annotations.mock(
        Ord.ConsumptionBundleReference.class,
        Map.ofEntries(Map.entry("defaultEntryPoint", "https://test-entry-point.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ConsumptionBundleReference annotation = Annotations.mock(
        Ord.ConsumptionBundleReference.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":consumptionBundle:test:v1")));

    assertEquals(
        new ConsumptionBundleReference().withOrdId(NAMESPACE + ":consumptionBundle:test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ConsumptionBundleReference annotation = Annotations.mock(
        Ord.ConsumptionBundleReference.class,
        Map.ofEntries(
            Map.entry("ordId", NAMESPACE + ":consumptionBundle:test:v1"),
            Map.entry("defaultEntryPoint", "https://test-entry-point.dummy.nowhere.org")));

    assertEquals(
        new ConsumptionBundleReference()
            .withOrdId(NAMESPACE + ":consumptionBundle:test:v1")
            .withDefaultEntryPoint("https://test-entry-point.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
