package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Tombstone;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class TombstoneGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.Tombstone, Tombstone> customizer;

  private EntityAutoGenerator<Ord.Tombstone, Tombstone> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(Tombstone::new);

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(7, Ord.Tombstone.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatRemovalDateIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(
            Context.of(Annotations.mock(Ord.Tombstone.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Tombstone annotation =
        Annotations.mock(Ord.Tombstone.class, Map.ofEntries(Map.entry("removalDate", "2025-03-25T14:30:00Z")));
    Context<Ord.Tombstone> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Tombstone().withRemovalDate(Commons.asDate("2025-03-25T14:30:00Z")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Tombstone annotation = Annotations.mock(
        Ord.Tombstone.class,
        Map.ofEntries(
            Map.entry("groupId", "test-group-id"),
            Map.entry("description", "test-description"),
            Map.entry("groupTypeId", "test-group-type-id"),
            Map.entry("removalDate", "2025-03-25T14:30:00Z"),
            Map.entry("ordId", "customer.test.namespace:apiResource:Test:v1")));
    Context<Ord.Tombstone> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Tombstone()
            .withGroupId("test-group-id")
            .withDescription("test-description")
            .withGroupTypeId("test-group-type-id")
            .withOrdId("customer.test.namespace:apiResource:Test:v1")
            .withRemovalDate(Commons.asDate("2025-03-25T14:30:00Z")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
