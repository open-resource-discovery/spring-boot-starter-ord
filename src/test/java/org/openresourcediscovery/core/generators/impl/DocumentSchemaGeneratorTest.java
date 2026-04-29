package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentSchema.OpenResourceDiscovery;
import org.openresourcediscovery.model.Group;
import org.openresourcediscovery.model.GroupType;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.Product;
import org.openresourcediscovery.model.SystemInstance;
import org.openresourcediscovery.model.SystemType;
import org.openresourcediscovery.model.SystemVersion;
import org.openresourcediscovery.model.Tombstone;
import org.openresourcediscovery.model.Vendor;
import org.openresourcediscovery.testutils.Annotations;
import org.openresourcediscovery.utils.Commons;

@ExtendWith(MockitoExtension.class)
class DocumentSchemaGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";
  private static final String DOCUMENT_SCHEMA_URL =
      "https://open-resource-discovery.github.io/specification/spec-v1/interfaces/Document.schema.json";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private DocumentSchemaGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new DocumentSchemaGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Group.class, new GroupGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Vendor.class, new VendorGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Product.class, new ProductGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Package.class, new PackageGenerator());
    prepareEntityGeneratorFactoryMock(Ord.GroupType.class, new GroupTypeGenerator());
    prepareEntityGeneratorFactoryMock(Ord.SystemType.class, new SystemTypeGenerator());
    prepareEntityGeneratorFactoryMock(Ord.SystemVersion.class, new SystemVersionGenerator());
    prepareEntityGeneratorFactoryMock(Ord.SystemInstance.class, new SystemInstanceGenerator());
    prepareEntityGeneratorFactoryMock(Ord.ConsumptionBundle.class, new ConsumptionBundleGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Tombstone.class, new EntityAutoGenerator<>(Tombstone::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(15, Ord.Document.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new DocumentSchema()
            .withPerspective(null) // TODO - to be fixed
            .with$schema(DOCUMENT_SCHEMA_URL)
            .withOpenResourceDiscovery(OpenResourceDiscovery._1_14),
        classUnderTest.generate(
            Context.of(Annotations.mock(Ord.Document.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Document annotation = Annotations.mock(
        Ord.Document.class,
        Map.ofEntries(
            Map.entry("groups", new Ord.Group[] {createGroupAnnotationMock()}),
            Map.entry("describedSystemType", createSystemTypeAnnotationMock()),
            Map.entry("vendors", new Ord.Vendor[] {createVendorAnnotationMock()}),
            Map.entry("products", new Ord.Product[] {createProductAnnotationMock()}),
            Map.entry("packages", new Ord.Package[] {createPackageAnnotationMock()}),
            Map.entry("describedSystemVersion", createSystemVersionAnnotationMock()),
            Map.entry("describedSystemInstance", createSystemInstanceAnnotationMock()),
            Map.entry("groupTypes", new Ord.GroupType[] {createGroupTypeAnnotationMock()}),
            Map.entry("tombstones", new Ord.Tombstone[] {createTombstoneAnnotationMock()}),
            Map.entry(
                "consumptionBundles",
                new Ord.ConsumptionBundle[] {createConsumptionBundleAnnotationMock()})));

    assertEquals(
        new DocumentSchema()
            .withPerspective(null) // TODO - to be fixed
            .with$schema(DOCUMENT_SCHEMA_URL)
            .withOpenResourceDiscovery(OpenResourceDiscovery._1_14)
            .withDescribedSystemType(new SystemType().withSystemNamespace(NAMESPACE))
            .withDescribedSystemVersion(
                new SystemVersion().withVersion("1.0.0").withTitle("Test Version"))
            .withDescribedSystemInstance(
                new SystemInstance().withBaseUrl("https://test-instance.dummy.nowhere.org"))
            .withVendors(List.of(
                new Vendor().withTitle("Test Vendor").withOrdId(NAMESPACE + ":vendor:TestVendor:v1")))
            .withProducts(List.of(new Product()
                .withTitle("Test Product")
                .withVendor("customer:vendor:Customer:")
                .withOrdId(NAMESPACE + ":product:TestProduct:v1")
                .withShortDescription(
                    "Auto-generated short description for DocumentSchemaGeneratorTest")))
            .withGroupTypes(List.of(new GroupType()
                .withTitle("Test GroupType")
                .withGroupTypeId(NAMESPACE + ":testgrouptype")))
            .withPackages(List.of(new Package()
                .withVersion("1.0.0")
                .withTitle("Test Package")
                .withVendor("customer:vendor:Customer:")
                .withOrdId(NAMESPACE + ":package:TestPackage:v1")
                .withDescription("Auto-generated description for DocumentSchemaGeneratorTest")
                .withShortDescription(
                    "Auto-generated short description for DocumentSchemaGeneratorTest")))
            .withConsumptionBundles(List.of(new ConsumptionBundle()
                .withTitle("Test Bundle")
                .withOrdId(NAMESPACE + ":consumptionBundle:TestBundle:v1")))
            .withTombstones(List.of(new Tombstone()
                .withOrdId(NAMESPACE + ":apiResource:Test:v1")
                .withRemovalDate(Commons.asDate("2025-01-01T00:00:00Z"))))
            .withGroups(List.of(new Group()
                .withTitle("Test Group")
                .withGroupTypeId(NAMESPACE + ":testgroup")
                .withGroupId(NAMESPACE + ":testgroup:" + NAMESPACE + ":DocumentSchemaGeneratorTest"))),
        classUnderTest.generate(Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.Group createGroupAnnotationMock() {
    return Annotations.mock(
        Ord.Group.class,
        Map.ofEntries(Map.entry("title", "Test Group"), Map.entry("groupTypeId", NAMESPACE + ":testgroup")));
  }

  private static Ord.Vendor createVendorAnnotationMock() {
    return Annotations.mock(
        Ord.Vendor.class,
        Map.ofEntries(
            Map.entry("title", "Test Vendor"), Map.entry("ordId", NAMESPACE + ":vendor:TestVendor:v1")));
  }

  private static Ord.Product createProductAnnotationMock() {
    return Annotations.mock(
        Ord.Product.class,
        Map.ofEntries(
            Map.entry("title", "Test Product"), Map.entry("ordId", NAMESPACE + ":product:TestProduct:v1")));
  }

  private static Ord.Package createPackageAnnotationMock() {
    return Annotations.mock(
        Ord.Package.class,
        Map.ofEntries(
            Map.entry("title", "Test Package"),
            Map.entry("version", "1.0.0"),
            Map.entry("ordId", NAMESPACE + ":package:TestPackage:v1")));
  }

  private static Ord.GroupType createGroupTypeAnnotationMock() {
    return Annotations.mock(
        Ord.GroupType.class,
        Map.ofEntries(
            Map.entry("title", "Test GroupType"), Map.entry("groupTypeId", NAMESPACE + ":testgrouptype")));
  }

  private static Ord.Tombstone createTombstoneAnnotationMock() {
    return Annotations.mock(
        Ord.Tombstone.class,
        Map.ofEntries(
            Map.entry("ordId", NAMESPACE + ":apiResource:Test:v1"),
            Map.entry("removalDate", "2025-01-01T00:00:00Z")));
  }

  private static Ord.SystemType createSystemTypeAnnotationMock() {
    return Annotations.mock(Ord.SystemType.class, Map.ofEntries(Map.entry("systemNamespace", NAMESPACE)));
  }

  private static Ord.SystemVersion createSystemVersionAnnotationMock() {
    return Annotations.mock(
        Ord.SystemVersion.class,
        Map.ofEntries(Map.entry("version", "1.0.0"), Map.entry("title", "Test Version")));
  }

  private static Ord.SystemInstance createSystemInstanceAnnotationMock() {
    return Annotations.mock(
        Ord.SystemInstance.class,
        Map.ofEntries(Map.entry("baseUrl", "https://test-instance.dummy.nowhere.org")));
  }

  private static Ord.ConsumptionBundle createConsumptionBundleAnnotationMock() {
    return Annotations.mock(
        Ord.ConsumptionBundle.class,
        Map.ofEntries(
            Map.entry("title", "Test Bundle"),
            Map.entry("ordId", NAMESPACE + ":consumptionBundle:TestBundle:v1")));
  }
}
