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
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ApiResourceIntegrationAspectSubsetGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.ApiResourceIntegrationAspectSubset, ApiResourceIntegrationAspectSubset>
      customizer;

  private EntityAutoGenerator<Ord.ApiResourceIntegrationAspectSubset, ApiResourceIntegrationAspectSubset>
      classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ApiResourceIntegrationAspectSubset::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(2, Ord.ApiResourceIntegrationAspectSubset.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOperationIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.ApiResourceIntegrationAspectSubset.class),
            getClass(),
            new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceIntegrationAspectSubset annotation = Annotations.mock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("operationId", "test-operation-id")));
    Context<Ord.ApiResourceIntegrationAspectSubset> context =
        Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new ApiResourceIntegrationAspectSubset().withOperationId("test-operation-id"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
