package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class DocumentationLabelsGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.DocumentationLabels, DocumentationLabels> customizer;

  private DocumentationLabelsGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new DocumentationLabelsGenerator();

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.DocumentationLabels.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenNullIsReturned() {
    Context<Ord.DocumentationLabels> context =
        Context.of(Annotations.mock(Ord.DocumentationLabels.class), getClass(), new DocumentSchema());

    assertNull(classUnderTest.generate(context));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DocumentationLabels annotation =
        Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
          Annotations.mock(
              Ord.DocumentationLabelsEntry.class,
              Map.ofEntries(
                  Map.entry("key", "key-1"),
                  Map.entry("values", new String[] {"value-1a", "value-1b"}))),
          Annotations.mock(
              Ord.DocumentationLabelsEntry.class,
              Map.ofEntries(Map.entry("key", "key-2"), Map.entry("values", new String[] {"value-2a"})))
        }));
    Context<Ord.DocumentationLabels> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new DocumentationLabels()
            .withAdditionalProperty("key-2", List.of("value-2a"))
            .withAdditionalProperty("key-1", List.of("value-1a", "value-1b")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
