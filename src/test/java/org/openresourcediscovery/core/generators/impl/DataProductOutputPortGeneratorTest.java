package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DataProductOutputPort;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class DataProductOutputPortGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.DataProductOutputPort, DataProductOutputPort> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(DataProductOutputPort::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.DataProductOutputPort.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.DataProductOutputPort.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductOutputPort annotation = Annotations.mock(
        Ord.DataProductOutputPort.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":apiResource:Test:outputPort:v1")));

    assertEquals(
        new DataProductOutputPort().withOrdId(NAMESPACE + ":apiResource:Test:outputPort:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
