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
import org.openresourcediscovery.model.DataProductLink;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class DataProductLinkGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.DataProductLink, DataProductLink> customizer;

  private EntityAutoGenerator<Ord.DataProductLink, DataProductLink> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(DataProductLink::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.DataProductLink.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.DataProductLink annotation =
        Annotations.mock(Ord.DataProductLink.class, Map.ofEntries(Map.entry("type", "custom")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(
            Map.entry("type", "api-documentation"),
            Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));
    Context<Ord.DataProductLink> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new DataProductLink()
            .withType("api-documentation")
            .withUrl("https://test-data-product-link.dummy.nowhere.org"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.DataProductLink annotation = Annotations.mock(
        Ord.DataProductLink.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-data-product-link.dummy.nowhere.org")));
    Context<Ord.DataProductLink> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new DataProductLink()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withUrl("https://test-data-product-link.dummy.nowhere.org"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
