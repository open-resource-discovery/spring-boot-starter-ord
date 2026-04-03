package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.model.CredentialExchangeStrategy;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

class CredentialExchangeStrategyGeneratorTest {

  private EntityAutoGenerator<Ord.CredentialExchangeStrategy, CredentialExchangeStrategy> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(CredentialExchangeStrategy::new);
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

    assertEquals(
        new CredentialExchangeStrategy().withType("open"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
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

    assertEquals(
        new CredentialExchangeStrategy()
            .withType("custom")
            .withCustomType("test-custom-type")
            .withCustomDescription("test-custom-description")
            .withCallbackUrl(URI.create("https://test-callback.dummy.nowhere.org")),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }
}
