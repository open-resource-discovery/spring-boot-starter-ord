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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class ApiModelSelectorODataGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.ApiModelSelectorOData, Object> customizer;

  private static final String TEST_TYPE = "test_type";
  private static final String TEST_ENTITY_SET_NAME = "TestEntitySet";

  private ApiModelSelectorODataGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new ApiModelSelectorODataGenerator();

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.ApiModelSelectorOData.class.getDeclaredMethods().length);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectMapIsReturned() {
    Ord.ApiModelSelectorOData annotation = Annotations.mock(
        Ord.ApiModelSelectorOData.class,
        Map.ofEntries(Map.entry("type", TEST_TYPE), Map.entry("entitySetName", TEST_ENTITY_SET_NAME)));
    Context<Ord.ApiModelSelectorOData> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        Map.ofEntries(Map.entry("type", TEST_TYPE), Map.entry("entitySetName", TEST_ENTITY_SET_NAME)),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @EmptySource
  @ParameterizedTest
  @ValueSource(strings = {" ", "\t", "\n"})
  void givenTypeIsBlank_whenGenerateIsCalled_thenExceptionIsThrown(String input) {
    Ord.ApiModelSelectorOData annotation = Annotations.mock(
        Ord.ApiModelSelectorOData.class,
        Map.ofEntries(Map.entry("type", input), Map.entry("entitySetName", TEST_ENTITY_SET_NAME)));

    assertThrows(
        NullPointerException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @EmptySource
  @ParameterizedTest
  @ValueSource(strings = {" ", "\t", "\n"})
  void givenEntitySetNameIsBlank_whenGenerateIsCalled_thenExceptionIsThrown(String input) {
    Ord.ApiModelSelectorOData annotation = Annotations.mock(
        Ord.ApiModelSelectorOData.class,
        Map.ofEntries(Map.entry("type", TEST_TYPE), Map.entry("entitySetName", input)));

    assertThrows(
        NullPointerException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
