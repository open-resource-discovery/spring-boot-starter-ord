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
import org.openresourcediscovery.model.CredentialExchangeStrategy;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class CredentialExchangeStrategyGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.CredentialExchangeStrategy, CredentialExchangeStrategy> customizer;

  private EntityAutoGenerator<Ord.CredentialExchangeStrategy, CredentialExchangeStrategy> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(CredentialExchangeStrategy::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(5, Ord.CredentialExchangeStrategy.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.CredentialExchangeStrategy.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.CredentialExchangeStrategy annotation =
        Annotations.mock(Ord.CredentialExchangeStrategy.class, Map.ofEntries(Map.entry("type", "open")));
    Context<Ord.CredentialExchangeStrategy> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(new CredentialExchangeStrategy().withType("open"), classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.CredentialExchangeStrategy annotation = Annotations.mock(
        Ord.CredentialExchangeStrategy.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("customDescription", "test-custom-description"),
            Map.entry("callbackUrl", "https://test-callback.dummy.nowhere.org")));
    Context<Ord.CredentialExchangeStrategy> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new CredentialExchangeStrategy()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withCustomDescription("test-custom-description")
            .withCallbackUrl(URI.create("https://test-callback.dummy.nowhere.org")),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
