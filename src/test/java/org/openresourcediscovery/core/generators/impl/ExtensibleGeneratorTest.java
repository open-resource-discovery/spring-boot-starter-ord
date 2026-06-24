package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Extensible;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ExtensibleGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.Extensible, Extensible> customizer;

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
    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.Extensible.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenNullIsReturned() {
    Context<Ord.Extensible> context =
        Context.of(Annotations.mock(Ord.Extensible.class), getClass(), new DocumentSchema());

    assertNull(classUnderTest.generate(context));
  }

  @Test
  void givenManualSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    Context<Ord.Extensible> context = Context.of(
        Annotations.mock(Ord.Extensible.class, Map.of("supported", "manual")),
        getClass(),
        new DocumentSchema());

    assertEquals(new Extensible().withSupported("manual"), classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenAutomaticSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    Context<Ord.Extensible> context = Context.of(
        Annotations.mock(Ord.Extensible.class, Map.of("supported", "automatic")),
        getClass(),
        new DocumentSchema());

    assertEquals(new Extensible().withSupported("automatic"), classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void givenNoSupported_whenGenerateIsCalled_thenEntityIsReturned() {
    Context<Ord.Extensible> context = Context.of(
        Annotations.mock(Ord.Extensible.class, Map.of("supported", "no")), getClass(), new DocumentSchema());

    assertEquals(new Extensible().withSupported("no"), classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Context<Ord.Extensible> context = Context.of(
        Annotations.mock(
            Ord.Extensible.class,
            Map.ofEntries(
                Map.entry("supported", "manual"),
                Map.entry("description", "test-extensible-description"))),
        getClass(),
        new DocumentSchema());

    assertEquals(
        new Extensible().withSupported("manual").withDescription("test-extensible-description"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
