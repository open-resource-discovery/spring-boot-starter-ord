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
import org.openresourcediscovery.model.ConsumptionBundleReference;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ConsumptionBundleReferenceGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.ConsumptionBundleReference, ConsumptionBundleReference> customizer;

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.ConsumptionBundleReference, ConsumptionBundleReference> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ConsumptionBundleReference::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
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
    Context<Ord.ConsumptionBundleReference> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new ConsumptionBundleReference().withOrdId(NAMESPACE + ":consumptionBundle:test:v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ConsumptionBundleReference annotation = Annotations.mock(
        Ord.ConsumptionBundleReference.class,
        Map.ofEntries(
            Map.entry("ordId", NAMESPACE + ":consumptionBundle:test:v1"),
            Map.entry("defaultEntryPoint", "https://test-entry-point.dummy.nowhere.org")));
    Context<Ord.ConsumptionBundleReference> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new ConsumptionBundleReference()
            .withOrdId(NAMESPACE + ":consumptionBundle:test:v1")
            .withDefaultEntryPoint("https://test-entry-point.dummy.nowhere.org"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
