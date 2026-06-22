package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.openresourcediscovery.model.CapabilityDefinition;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class CapabilityDefinitionGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.CapabilityDefinition, CapabilityDefinition> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityAutoGenerator<Ord.CapabilityDefinition, CapabilityDefinition> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(CapabilityDefinition::new);

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    prepareEntityGeneratorFactoryMock(Ord.AccessStrategy.class, new EntityAutoGenerator<>(AccessStrategy::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(8, Ord.CapabilityDefinition.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.CapabilityDefinition annotation = Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(
            Map.entry("mediaType", "application/json"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatMediaTypeIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.CapabilityDefinition annotation = Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(
            Map.entry("type", "custom"), Map.entry("url", "https://test-definition.dummy.nowhere.org")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatUrlIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    Ord.CapabilityDefinition annotation = Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(Map.entry("type", "custom"), Map.entry("mediaType", "application/json")));

    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.CapabilityDefinition annotation = Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("mediaType", "application/json"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org")));
    Context<Ord.CapabilityDefinition> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new CapabilityDefinition()
            .withType("custom")
            .withMediaType("application/json")
            .withUrl("https://test-definition.dummy.nowhere.org")
            .withAccessStrategies(null),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.CapabilityDefinition annotation = Annotations.mock(
        Ord.CapabilityDefinition.class,
        Map.ofEntries(
            Map.entry("type", "custom"),
            Map.entry("visibility", "public"),
            Map.entry("purpose", "ord:ai-enrichment"),
            Map.entry("mediaType", "application/json"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-definition.dummy.nowhere.org"),
            Map.entry(
                "accessStrategies", new Ord.AccessStrategy[] {createAccessStrategyAnnotationMock()})));
    Context<Ord.CapabilityDefinition> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new CapabilityDefinition()
            .withType("custom")
            .withVisibility("public")
            .withPurpose("ord:ai-enrichment")
            .withMediaType("application/json")
            .withCustomType("test-custom-type")
            .withUrl("https://test-definition.dummy.nowhere.org")
            .withAccessStrategies(List.of(new AccessStrategy()
                .withType("open")
                .withCustomType("test-access-strategy-custom-type")
                .withCustomDescription("test-access-strategy-custom-description"))),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
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
