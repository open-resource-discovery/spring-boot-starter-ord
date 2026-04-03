package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class EntityTypeOrdIdTargetGeneratorTest {

  private static final String TEST_ORD_ID = "customer.ns:entityType:MyEntity:v1";

  @Mock
  private DocumentSchema documentSchema;

  private EntityTypeOrdIdTargetGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityTypeOrdIdTargetGenerator();
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.EntityTypeOrdIdTarget.class.getDeclaredMethods().length);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectMapIsReturned() {
    Ord.EntityTypeOrdIdTarget annotation =
        Annotations.mock(Ord.EntityTypeOrdIdTarget.class, Map.of("ordId", TEST_ORD_ID));

    assertEquals(
        Map.of("ordId", TEST_ORD_ID),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @EmptySource
  @ParameterizedTest
  @ValueSource(strings = {" ", "\t", "\n"})
  void givenOrdIdIsBlank_whenGenerateIsCalled_thenExceptionIsThrown(String input) {
    Ord.EntityTypeOrdIdTarget annotation =
        Annotations.mock(Ord.EntityTypeOrdIdTarget.class, Map.of("ordId", input));

    assertThrows(
        NullPointerException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), documentSchema)));
  }
}
