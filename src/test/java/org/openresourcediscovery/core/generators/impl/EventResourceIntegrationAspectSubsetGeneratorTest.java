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
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class EventResourceIntegrationAspectSubsetGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.EventResourceIntegrationAspectSubset, EventResourceIntegrationAspectSubset>
      customizer;

  private EntityAutoGenerator<Ord.EventResourceIntegrationAspectSubset, EventResourceIntegrationAspectSubset>
      classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(EventResourceIntegrationAspectSubset::new);

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.EventResourceIntegrationAspectSubset.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatEventTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.EventResourceIntegrationAspectSubset.class),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EventResourceIntegrationAspectSubset annotation = Annotations.mock(
        Ord.EventResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("eventType", "test-event-type")));
    Context<Ord.EventResourceIntegrationAspectSubset> context =
        Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EventResourceIntegrationAspectSubset().withEventType("test-event-type"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
