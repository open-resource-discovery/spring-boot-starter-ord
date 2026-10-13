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
import org.openresourcediscovery.model.SystemTypeIntegrationAspect;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class SystemTypeIntegrationAspectGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.SystemTypeIntegrationAspect, SystemTypeIntegrationAspect> customizer;

  private static final String SYSTEM_NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.SystemTypeIntegrationAspect, SystemTypeIntegrationAspect> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(SystemTypeIntegrationAspect::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.SystemTypeIntegrationAspect.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatSystemNamespaceIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.SystemTypeIntegrationAspect.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.SystemTypeIntegrationAspect annotation = Annotations.mock(
        Ord.SystemTypeIntegrationAspect.class, Map.ofEntries(Map.entry("systemNamespace", SYSTEM_NAMESPACE)));
    Context<Ord.SystemTypeIntegrationAspect> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new SystemTypeIntegrationAspect().withSystemNamespace(SYSTEM_NAMESPACE),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.SystemTypeIntegrationAspect annotation = Annotations.mock(
        Ord.SystemTypeIntegrationAspect.class,
        Map.ofEntries(Map.entry("minSystemVersion", "1.0.0"), Map.entry("systemNamespace", SYSTEM_NAMESPACE)));
    Context<Ord.SystemTypeIntegrationAspect> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new SystemTypeIntegrationAspect().withMinSystemVersion("1.0.0").withSystemNamespace(SYSTEM_NAMESPACE),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
