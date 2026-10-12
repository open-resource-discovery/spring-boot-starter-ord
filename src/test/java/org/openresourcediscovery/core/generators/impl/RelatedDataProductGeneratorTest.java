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
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.RelatedDataProduct;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.testutils.TestObjectProvider;

@ExtendWith(MockitoExtension.class)
class RelatedDataProductGeneratorTest {

  @Mock
  private EntityGenerator.Customizer<Ord.RelatedDataProduct, RelatedDataProduct> customizer;

  private static final String NAMESPACE = "customer.test.namespace";

  private EntityAutoGenerator<Ord.RelatedDataProduct, RelatedDataProduct> classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new EntityAutoGenerator<>(RelatedDataProduct::new) {};

    classUnderTest.setCustomizers(new TestObjectProvider<>(customizer));

    lenient().when(customizer.customize(any(), any())).then(in -> in.getArguments()[1]);
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(3, Ord.RelatedDataProduct.class.getDeclaredMethods().length);
  }

  @Test
  void givenThatOrdIdIsMissing_whenGenerateIsCalled_thenExceptionIsThrown() {
    assertThrows(
        IllegalStateException.class,
        () -> classUnderTest.generate(
            Context.of(Annotations.mock(Ord.RelatedDataProduct.class), getClass(), new DocumentSchema())));
  }

  @Test
  void givenThatOnlyRequiredFieldsAreProvided_whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.RelatedDataProduct annotation = Annotations.mock(
        Ord.RelatedDataProduct.class, Map.ofEntries(Map.entry("ordId", NAMESPACE + ":dataProduct:Test:v1")));
    Context<Ord.RelatedDataProduct> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new RelatedDataProduct().withOrdId(NAMESPACE + ":dataProduct:Test:v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.RelatedDataProduct annotation = Annotations.mock(
        Ord.RelatedDataProduct.class,
        Map.ofEntries(
            Map.entry("relationType", "test-relation-type"),
            Map.entry("ordId", NAMESPACE + ":dataProduct:Test:v1")));
    Context<Ord.RelatedDataProduct> context = Context.of(annotation, getClass(), new DocumentSchema());

    assertEquals(
        new RelatedDataProduct()
            .withRelationType("test-relation-type")
            .withOrdId(NAMESPACE + ":dataProduct:Test:v1"),
        classUnderTest.generate(context));

    verify(customizer).customize(eq(context), any());
    verifyNoMoreInteractions(customizer);
  }
}
