package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class LabelsGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.Labels, Labels> customizer;

  private LabelsGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new LabelsGenerator();

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.Labels.class.getDeclaredMethods().length);
  }

  @Test
  void givenEmptyAnnotationValues_whenGenerateIsCalled_thenNullIsReturned() {
    Context<Ord.Labels> context = Context.of(mock(Ord.Labels.class), getClass(), new DocumentSchema());

    assertNull(classUnderTest.generate(context));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Labels annotation = Annotations.mock(
        Ord.Labels.class, Map.of("value", new Ord.LabelsEntry[] {createLabelsEntryAnnotationMock()}));
    Context<Ord.Labels> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Labels()
            .withAdditionalProperty("test-label-key", List.of("test-label-value-1", "test-label-value-2")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  private static Ord.LabelsEntry createLabelsEntryAnnotationMock() {
    return Annotations.mock(
        Ord.LabelsEntry.class,
        Map.ofEntries(
            Map.entry("key", "test-label-key"),
            Map.entry("values", new String[] {"test-label-value-1", "test-label-value-2"})));
  }
}
