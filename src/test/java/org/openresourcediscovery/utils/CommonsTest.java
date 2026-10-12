package org.openresourcediscovery.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CommonsTest {

  @Retention(RetentionPolicy.RUNTIME)
  @interface SampleAnnotation {
    String value() default "";
  }

  @SampleAnnotation
  static class DefaultAnnotated {}

  @SampleAnnotation("non-default")
  static class NonDefaultAnnotated {}

  // -------------------------------------------------------------------------
  // isEmpty(A annotation)
  // -------------------------------------------------------------------------

  @Test
  void givenNullAnnotation_whenIsEmptyIsCalled_thenTrueIsReturned() {
    assertTrue(Commons.isEmpty((SampleAnnotation) null));
  }

  @Test
  void givenAnnotationWithAllDefaultValues_whenIsEmptyIsCalled_thenTrueIsReturned() {
    SampleAnnotation annotation = DefaultAnnotated.class.getAnnotation(SampleAnnotation.class);

    assertTrue(Commons.isEmpty(annotation));
  }

  @Test
  void givenAnnotationWithNonDefaultValue_whenIsEmptyIsCalled_thenFalseIsReturned() {
    SampleAnnotation annotation = NonDefaultAnnotated.class.getAnnotation(SampleAnnotation.class);

    assertFalse(Commons.isEmpty(annotation));
  }

  // -------------------------------------------------------------------------
  // asDate(String)
  // -------------------------------------------------------------------------

  @Test
  void givenNullString_whenAsDateIsCalled_thenNullIsReturned() {
    assertNull(Commons.asDate(null));
  }

  @Test
  void givenEmptyString_whenAsDateIsCalled_thenNullIsReturned() {
    assertNull(Commons.asDate(""));
  }

  @Test
  void givenValidIsoDateTimeString_whenAsDateIsCalled_thenDateIsReturned() {
    Date result = Commons.asDate("2024-01-15T00:00:00");

    assertNotNull(result);
  }

  @Test
  void givenValidIsoDateTimeString_whenAsDateIsCalled_thenCorrectDateIsReturned() {
    Date result = Commons.asDate("2024-01-15T00:00:00");

    // Verify the date represents January 15, 2024
    @SuppressWarnings("deprecation")
    int year = result.getYear() + 1900;
    @SuppressWarnings("deprecation")
    int month = result.getMonth() + 1;
    @SuppressWarnings("deprecation")
    int day = result.getDate();

    assertEquals(2024, year);
    assertEquals(1, month);
    assertEquals(15, day);
  }

  // -------------------------------------------------------------------------
  // asList(T...)
  // -------------------------------------------------------------------------

  @Test
  void givenNullArray_whenAsListIsCalled_thenNullIsReturned() {
    assertNull(Commons.asList((String[]) null));
  }

  @Test
  void givenEmptyArray_whenAsListIsCalled_thenNullIsReturned() {
    assertNull(Commons.asList(new String[0]));
  }

  @Test
  void givenSingleElement_whenAsListIsCalled_thenListWithOneElementIsReturned() {
    List<String> result = Commons.asList("a");

    assertEquals(List.of("a"), result);
  }

  @Test
  void givenMultipleElements_whenAsListIsCalled_thenListWithAllElementsIsReturned() {
    List<String> result = Commons.asList("a", "b", "c");

    assertEquals(List.of("a", "b", "c"), result);
  }

  // -------------------------------------------------------------------------
  // asSet(T...)
  // -------------------------------------------------------------------------

  @Test
  void givenNullArray_whenAsSetIsCalled_thenNullIsReturned() {
    assertNull(Commons.asSet((String[]) null));
  }

  @Test
  void givenEmptyArray_whenAsSetIsCalled_thenNullIsReturned() {
    assertNull(Commons.asSet(new String[0]));
  }

  @Test
  void givenSingleElement_whenAsSetIsCalled_thenSetWithOneElementIsReturned() {
    Set<String> result = Commons.asSet("a");

    assertEquals(Set.of("a"), result);
  }

  @Test
  void givenMultipleElements_whenAsSetIsCalled_thenSetWithAllElementsIsReturned() {
    Set<String> result = Commons.asSet("a", "b", "c");

    assertEquals(Set.of("a", "b", "c"), result);
  }

  // -------------------------------------------------------------------------
  // asCollection(T...)
  // -------------------------------------------------------------------------

  @Test
  void givenNullArray_whenAsCollectionIsCalled_thenNullIsReturned() {
    assertNull(Commons.asCollection(Set.class, (String[]) null));
    assertNull(Commons.asCollection(List.class, (String[]) null));
  }

  @Test
  void givenEmptyArray_whenAsCollectionIsCalled_thenNullIsReturned() {
    assertNull(Commons.asCollection(Set.class, new String[0]));
    assertNull(Commons.asCollection(List.class, new String[0]));
  }

  @Test
  void givenSingleElement_whenAsCollectionIsCalled_thenSetWithOneElementIsReturned() {
    assertEquals(Set.of("a"), Commons.asCollection(Set.class, "a"));
    assertEquals(List.of("a"), Commons.asCollection(List.class, "a"));
  }

  @Test
  void givenMultipleElements_whenAsCollectionIsCalled_thenSetWithAllElementsIsReturned() {
    assertEquals(Set.of("a", "b", "c"), Commons.asCollection(Set.class, "a", "b", "c"));
    assertEquals(List.of("a", "b", "c"), Commons.asCollection(List.class, "a", "b", "c"));
  }

  @Test
  void givenUnsupportedCollectionType_whenAsCollectionIsCalled_thenExceptionIsThrown() {
    assertThrows(IllegalArgumentException.class, () -> Commons.asCollection(Queue.class, "a", "b", "c"));
  }
}
