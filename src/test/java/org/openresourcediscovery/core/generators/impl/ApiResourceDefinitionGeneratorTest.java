package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.AccessStrategy;
import org.openresourcediscovery.model.ApiResourceDefinition;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class ApiResourceDefinitionGeneratorTest {

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityAutoGenerator<Ord.ApiResourceDefinition, ApiResourceDefinition> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ApiResourceDefinition::new);

    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(7, Ord.ApiResourceDefinition.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ApiResourceDefinition annotation = Annotations.mock(
        Ord.ApiResourceDefinition.class,
        Map.ofEntries(
            Map.entry("mediaType", "application/json"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatMediaTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ApiResourceDefinition annotation = Annotations.mock(
        Ord.ApiResourceDefinition.class,
        Map.ofEntries(
            Map.entry("type", "openapi-v3"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.ApiResourceDefinition annotation = Annotations.mock(
        Ord.ApiResourceDefinition.class,
        Map.ofEntries(Map.entry("type", "openapi-v3"), Map.entry("mediaType", "application/json")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceDefinition annotation = Annotations.mock(
        Ord.ApiResourceDefinition.class,
        Map.ofEntries(
            Map.entry("type", "openapi-v3"),
            Map.entry("mediaType", "application/json"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org")));

    assertEquals(
        new ApiResourceDefinition()
            .withType("openapi-v3")
            .withMediaType("application/json")
            .withUrl("https://test-definition.dummy.nowhere.org"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceDefinition annotation = Annotations.mock(
        Ord.ApiResourceDefinition.class,
        Map.ofEntries(
            Map.entry("type", "openapi-v3"),
            Map.entry("visibility", "public"),
            Map.entry("mediaType", "application/json"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org"),
            Map.entry(
                "accessStrategies", new Ord.AccessStrategy[] {createAccessStrategyAnnotationMock()})));

    assertEquals(
        new ApiResourceDefinition()
            .withType("openapi-v3")
            .withVisibility("public")
            .withMediaType("application/json")
            .withCustomType("test-custom-type")
            .withUrl("https://test-definition.dummy.nowhere.org")
            .withAccessStrategies(List.of(new AccessStrategy()
                .withType("open")
                .withCustomType("test-access-strategy-custom-type")
                .withCustomDescription("test-access-strategy-custom-description"))),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.AccessStrategy createAccessStrategyAnnotationMock() {
    return Annotations.mock(
        Ord.AccessStrategy.class,
        Map.ofEntries(
            Map.entry("type", "open"),
            Map.entry("customType", "test-access-strategy-custom-type"),
            Map.entry("customDescription", "test-access-strategy-custom-description")));
  }
}
