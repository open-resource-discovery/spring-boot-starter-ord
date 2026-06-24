package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
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
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class LinkGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.Link, Link> customizer;

  private EntityAutoGenerator<Ord.Link, Link> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(Link::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.Link.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTitleIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class, Map.ofEntries(Map.entry("url", "https://test-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.Link annotation = Annotations.mock(Ord.Link.class, Map.ofEntries(Map.entry("title", "test-link-title")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));
    Context<Ord.Link> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Link().withTitle("test-link-title").withUrl(URI.create("https://test-link.dummy.nowhere.org")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Link annotation = Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("description", "test-link-description"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));
    Context<Ord.Link> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new Link()
            .withTitle("test-link-title")
            .withDescription("test-link-description")
            .withUrl(URI.create("https://test-link.dummy.nowhere.org")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
