package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DataProductInputPort;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class DataProductInputPortGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.DataProductInputPort, DataProductInputPort> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(DataProductInputPort::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.DataProductInputPort.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.DataProductInputPort.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductInputPort annotation = Annotations.mock(
        Ord.DataProductInputPort.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":integrationDependency:Test:inputPort:v1")));

    assertEquals(
        new DataProductInputPort().withOrdId(NAMESPACE + ":integrationDependency:Test:inputPort:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
