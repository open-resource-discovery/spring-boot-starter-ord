package org.openresourcediscovery.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.impl.fixtures.MultiDocumentAnnotatedClass;
import org.openresourcediscovery.core.services.impl.fixtures.SingleDocumentAnnotatedClass;
import org.openresourcediscovery.core.services.impl.fixtures.VendorAnnotatedClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class CachingOrdAnnotationsScannerImplTest {

  private static final String FIXTURES_PACKAGE = "org.openresourcediscovery.core.services.impl.fixtures";
  private static final String EMPTY_PACKAGE = "org.openresourcediscovery.core.services.impl.empty";

  private CachingOrdAnnotationsScannerImpl classUnderTest;
  private AnnotationConfigApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    applicationContext = new AnnotationConfigApplicationContext();
  }

  @AfterEach
  void tearDown() {
    applicationContext.close();
  }

  @Test
  void givenEmptyPackageList_whenScanIsCalled_thenEmptyListIsReturned() {
    assertTrue(scanner().scan(Ord.Vendor.class).isEmpty());
  }

  @Test
  void givenPackageWithNoMatchingAnnotation_whenScanIsCalled_thenEmptyListIsReturned() {
    assertTrue(scanner(EMPTY_PACKAGE).scan(Ord.Vendor.class).isEmpty());
  }

  @Test
  void givenClassWithVendorAnnotation_whenScanIsCalled_thenClassIsFound() {
    List<Pair<? extends Class<?>, Ord.Vendor>> result =
        scanner(FIXTURES_PACKAGE).scan(Ord.Vendor.class);

    assertEquals(1, result.size());
    assertEquals(VendorAnnotatedClass.class, result.get(0).getLeft());
  }

  @Test
  void givenClassWithVendorAnnotation_whenScanIsCalled_thenAnnotationValuesAreCorrect() {
    List<Pair<? extends Class<?>, Ord.Vendor>> result =
        scanner(FIXTURES_PACKAGE).scan(Ord.Vendor.class);

    assertEquals("Test Vendor", result.get(0).getRight().title());
    assertEquals("customer:vendor:test:v1", result.get(0).getRight().ordId());
  }

  @Test
  void givenClassWithSingleRepeatableAnnotation_whenScanIsCalled_thenOneEntryIsReturned() {
    assertEquals(
        1,
        scanner(FIXTURES_PACKAGE).scan(Ord.Document.class).stream()
            .filter(p -> p.getLeft().equals(SingleDocumentAnnotatedClass.class))
            .count());
  }

  @Test
  void givenClassWithSingleRepeatableAnnotation_whenScanIsCalled_thenAnnotationIdIsCorrect() {
    assertEquals(
        "doc-single",
        scanner(FIXTURES_PACKAGE).scan(Ord.Document.class).stream()
            .filter(p -> p.getLeft().equals(SingleDocumentAnnotatedClass.class))
            .map(p -> p.getRight().id())
            .findFirst()
            .orElseThrow());
  }

  @Test
  void givenClassWithRepeatedAnnotation_whenScanIsCalledForContainerType_thenContainerIsFound() {
    assertEquals(
        2,
        scanner(FIXTURES_PACKAGE).scan(Ord.Document.class).stream()
            .filter(p -> p.getLeft().equals(MultiDocumentAnnotatedClass.class))
            .count());
  }

  @Test
  void givenClassWithRepeatedAnnotation_whenScanIsCalledForContainerType_thenBothAnnotationIdsArePresent() {
    assertEquals(
        Set.of("doc-1", "doc-2"),
        scanner(FIXTURES_PACKAGE).scan(Ord.Document.class).stream()
            .filter(p -> p.getLeft().equals(MultiDocumentAnnotatedClass.class))
            .map(found -> found.getRight().id())
            .collect(Collectors.toSet()));
  }

  @Test
  void givenMultiplePackages_whenScanIsCalled_thenAllPackagesAreScanned() {
    assertEquals(
        1,
        scanner(EMPTY_PACKAGE, FIXTURES_PACKAGE).scan(Ord.Vendor.class).size());
  }

  @Test
  void whenScanIsCalledTwiceForSameAnnotation_thenSameResultIsReturned() {
    classUnderTest = scanner(FIXTURES_PACKAGE);

    assertEquals(classUnderTest.scan(Ord.Vendor.class), classUnderTest.scan(Ord.Vendor.class));
  }

  @Test
  void whenInitIsCalledBeforeScan_thenPrewarmedAnnotationsAreReturnedCorrectly() {
    classUnderTest = scanner(FIXTURES_PACKAGE);
    classUnderTest.init();

    List<Pair<? extends Class<?>, Ord.Vendor>> result = classUnderTest.scan(Ord.Vendor.class);

    assertEquals(1, result.size());
    assertEquals(VendorAnnotatedClass.class, result.get(0).getLeft());
  }

  private CachingOrdAnnotationsScannerImpl scanner(String... packages) {
    return new CachingOrdAnnotationsScannerImpl(
        OrdProperties.builder().packages(List.of(packages)).build(), applicationContext);
  }
}
