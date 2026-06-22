package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
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
import org.openresourcediscovery.model.PackageLink;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class PackageLinkGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.PackageLink, PackageLink> customizer;

  private EntityAutoGenerator<Ord.PackageLink, PackageLink> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(PackageLink::new);

    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.PackageLink.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.PackageLink annotation = Annotations.mock(
        Ord.PackageLink.class, Map.ofEntries(Map.entry("url", "https://test-package-link.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.PackageLink annotation =
        Annotations.mock(Ord.PackageLink.class, Map.ofEntries(Map.entry("type", "api-documentation")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.PackageLink annotation = Annotations.mock(
        Ord.PackageLink.class,
        Map.ofEntries(
            Map.entry("type", "api-documentation"),
            Map.entry("url", "https://test-package-link.dummy.nowhere.org")));
    Context<Ord.PackageLink> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new PackageLink()
            .withType("api-documentation")
            .withUrl(URI.create("https://test-package-link.dummy.nowhere.org")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.PackageLink annotation = Annotations.mock(
        Ord.PackageLink.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-package-link.dummy.nowhere.org")));
    Context<Ord.PackageLink> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new PackageLink()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withUrl(URI.create("https://test-package-link.dummy.nowhere.org")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
