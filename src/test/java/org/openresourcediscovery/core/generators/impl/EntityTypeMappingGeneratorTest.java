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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EntityTypeMapping;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class EntityTypeMappingGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private EntityGenerator.Customizer<Ord.EntityTypeMapping, EntityTypeMapping> customizer;

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityAutoGenerator<Ord.EntityTypeMapping, EntityTypeMapping> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(EntityTypeMapping::new);

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);
    classUnderTest.setCustomizers(List.of(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);

    prepareEntityGeneratorFactoryMock(Ord.ApiModelSelectorOData.class, new ApiModelSelectorODataGenerator());
    prepareEntityGeneratorFactoryMock(Ord.EntityTypeOrdIdTarget.class, new EntityTypeOrdIdTargetGenerator());
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.EntityTypeMapping.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatEntityTypeTargetsIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(
            Context.of(Annotations.mock(Ord.EntityTypeMapping.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EntityTypeMapping annotation = Annotations.mock(
        Ord.EntityTypeMapping.class,
        Map.ofEntries(Map.entry(
            "entityTypeTargets",
            new Ord.EntityTypeOrdIdTarget[] {createEntityTypeOrdIdTargetAnnotationMock()})));
    Context<Ord.EntityTypeMapping> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EntityTypeMapping()
            .withApiModelSelectors(null)
            .withEntityTypeTargets(List.of(Map.of("ordId", NAMESPACE + ":entityType:Test:v1"))),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.EntityTypeMapping annotation = Annotations.mock(
        Ord.EntityTypeMapping.class,
        Map.ofEntries(
            Map.entry(
                "entityTypeTargets",
                new Ord.EntityTypeOrdIdTarget[] {createEntityTypeOrdIdTargetAnnotationMock()}),
            Map.entry(
                "apiModelSelectors",
                new Ord.ApiModelSelectorOData[] {createApiModelSelectorODataAnnotationMock()})));
    Context<Ord.EntityTypeMapping> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new EntityTypeMapping()
            .withEntityTypeTargets(List.of(Map.of("ordId", NAMESPACE + ":entityType:Test:v1")))
            .withApiModelSelectors(List.of(Map.of("type", "odata", "entitySetName", "TestEntitySet"))),
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

  private static Ord.EntityTypeOrdIdTarget createEntityTypeOrdIdTargetAnnotationMock() {
    return Annotations.mock(
        Ord.EntityTypeOrdIdTarget.class, Map.ofEntries(Map.entry("ordId", NAMESPACE + ":entityType:Test:v1")));
  }

  private static Ord.ApiModelSelectorOData createApiModelSelectorODataAnnotationMock() {
    return Annotations.mock(
        Ord.ApiModelSelectorOData.class,
        Map.ofEntries(Map.entry("type", "odata"), Map.entry("entitySetName", "TestEntitySet")));
  }
}
