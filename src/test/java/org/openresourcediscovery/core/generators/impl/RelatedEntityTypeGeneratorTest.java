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
import org.openresourcediscovery.model.RelatedEntityType;
import org.openresourcediscovery.testutils.Annotations;

class RelatedEntityTypeGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.RelatedEntityType, RelatedEntityType> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(RelatedEntityType::new);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.RelatedEntityType.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(
            Context.of(Annotations.mock(Ord.RelatedEntityType.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.RelatedEntityType annotation = Annotations.mock(
        Ord.RelatedEntityType.class, Map.ofEntries(Map.entry("ordId", NAMESPACE + ":entityType:Test:v1")));

    assertEquals(
        new RelatedEntityType().withOrdId(NAMESPACE + ":entityType:Test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.RelatedEntityType annotation = Annotations.mock(
        Ord.RelatedEntityType.class,
        Map.ofEntries(
            Map.entry("relationType", "test-relation-type"),
            Map.entry("ordId", NAMESPACE + ":entityType:Test:v1")));

    assertEquals(
        new RelatedEntityType()
            .withRelationType("test-relation-type")
            .withOrdId(NAMESPACE + ":entityType:Test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
