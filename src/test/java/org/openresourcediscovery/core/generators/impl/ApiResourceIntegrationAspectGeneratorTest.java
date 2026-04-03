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
import org.openresourcediscovery.model.ApiResourceIntegrationAspect;
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class ApiResourceIntegrationAspectGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private EntityAutoGenerator<Ord.ApiResourceIntegrationAspect, ApiResourceIntegrationAspect> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(ApiResourceIntegrationAspect::new);

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    prepareEntityGeneratorFactoryMock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        new EntityAutoGenerator<>(ApiResourceIntegrationAspectSubset::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(4, Ord.ApiResourceIntegrationAspect.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(Context.of(
            Annotations.mock(Ord.ApiResourceIntegrationAspect.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceIntegrationAspect annotation = Annotations.mock(
        Ord.ApiResourceIntegrationAspect.class,
        Map.ofEntries(Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1")));

    assertEquals(
        new ApiResourceIntegrationAspect().withOrdId(NAMESPACE + ":apiResource:Test:v1"),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.ApiResourceIntegrationAspect annotation = Annotations.mock(
        Ord.ApiResourceIntegrationAspect.class,
        Map.ofEntries(
            Map.entry("minVersion", "1.0.0"),
            Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1"),
            Map.entry("subset", new Ord.ApiResourceIntegrationAspectSubset[] {createSubsetAnnotationMock()
            })));

    assertEquals(
        new ApiResourceIntegrationAspect()
            .withMinVersion("1.0.0")
            .withOrdId(NAMESPACE + ":apiResource:Test:v1")
            .withSubset(
                List.of(new ApiResourceIntegrationAspectSubset().withOperationId("test-operation-id"))),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.ApiResourceIntegrationAspectSubset createSubsetAnnotationMock() {
    return Annotations.mock(
        Ord.ApiResourceIntegrationAspectSubset.class,
        Map.ofEntries(Map.entry("operationId", "test-operation-id")));
  }
}
