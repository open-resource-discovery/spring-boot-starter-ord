package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.openresourcediscovery.model.EventCompatibility;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class EventCompatibilityGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.EventCompatibility, EventCompatibility> customizer;

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.EventCompatibility, EventCompatibility> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(EventCompatibility::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.EventCompatibility.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.EventCompatibility annotation =
        Annotations.mock(Ord.EventCompatibility.class, Map.ofEntries(Map.entry("maxVersion", "1.0")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatMaxVersionIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.EventCompatibility annotation = Annotations.mock(
        Ord.EventCompatibility.class, Map.ofEntries(Map.entry("ordId", NAMESPACE + ":eventResource:Test:v1")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventCompatibility annotation = Annotations.mock(
        Ord.EventCompatibility.class,
        Map.ofEntries(
            Map.entry("maxVersion", "1.0"), Map.entry("ordId", NAMESPACE + ":eventResource:Test:v1")));
    Context<Ord.EventCompatibility> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EventCompatibility().withMaxVersion("1.0").withOrdId(NAMESPACE + ":eventResource:Test:v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
