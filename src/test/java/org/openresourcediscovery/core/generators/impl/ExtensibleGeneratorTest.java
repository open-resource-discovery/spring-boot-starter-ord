package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Extensible;
import org.openresourcediscovery.model.Extensible.Supported;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class ExtensibleGeneratorTest {

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private ExtensibleGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ExtensibleGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.Extensible.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenNullIsReturned() {
    assertNull(classUnderTest.generate(
        Context.of(Annotations.mock(Ord.Extensible.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenManualSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    assertEquals(
        new Extensible().withSupported(Supported.MANUAL),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.Extensible.class, Map.of("supported", "manual")),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void givenAutomaticSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    assertEquals(
        new Extensible().withSupported(Supported.AUTOMATIC),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.Extensible.class, Map.of("supported", "automatic")),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void givenNoSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    assertEquals(
        new Extensible().withSupported(Supported.NO),
        classUnderTest.generate(Context.of(
            Annotations.mock(Ord.Extensible.class, Map.of("supported", "no")),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void givenInvalidSupportedValueSupported_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalArgumentException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.Extensible.class, Map.of("supported", "invalid")),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    assertEquals(
        new Extensible().withSupported(Supported.MANUAL).withDescription("test-extensible-description"),
        classUnderTest.generate(Context.of(
            Annotations.mock(
                Ord.Extensible.class,
                Map.ofEntries(
                    Map.entry("supported", "manual"),
                    Map.entry("description", "test-extensible-description"))),
            getClass(),
            new DocumentSchema())));
  }
}
