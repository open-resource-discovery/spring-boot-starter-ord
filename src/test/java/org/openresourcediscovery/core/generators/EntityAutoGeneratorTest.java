package org.openresourcediscovery.core.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.generators.impl.ApiResourceGenerator;
import org.openresourcediscovery.core.generators.impl.LabelsGenerator;
import org.openresourcediscovery.core.generators.impl.VendorGenerator;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.ApiResource;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.File;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.PackageLink;
import org.openresourcediscovery.model.Vendor;

@ExtendWith(MockitoExtension.class)
class EntityAutoGeneratorTest {

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private VendorGenerator vendorGenerator;
  private ApiResourceGenerator apiResourceGenerator;

  @BeforeEach
  void setUp() {
    vendorGenerator = new VendorGenerator();
    vendorGenerator.setOrdProperties(ordProperties);
    vendorGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    apiResourceGenerator = new ApiResourceGenerator();
    apiResourceGenerator.setOrdProperties(ordProperties);
    apiResourceGenerator.setEntityGeneratorFactory(entityGeneratorFactory);
  }

  // ── Annotated fixtures ────────────────────────────────────────────────────

  @Ord.Vendor(ordId = "sap:vendor:SAP:v1", title = "SAP SE")
  static class WithPlainFields {}

  @Ord.Vendor(tags = {"cloud", "enterprise"})
  static class WithTags {}

  @Ord.Vendor(partners = {"sap:vendor:Partner:v1"})
  static class WithPartners {}

  @Ord.Vendor
  static class WithAllDefaults {}

  @Ord.Vendor(
      labels =
          @Ord.Labels(
              value = {
                @Ord.LabelsEntry(
                    key = "env",
                    values = {"prod", "dev"})
              }))
  static class WithLabels {}

  @Ord.ApiResource(lastUpdate = "2024-06-01T00:00:00")
  static class WithDateField {}

  @Ord.Package(
      ordId = "customer:package:TestPackage:v1",
      title = "Test Package",
      shortDescription = "A short description",
      description = "A description",
      version = "1.0.0",
      vendor = "customer:vendor:Customer:v1",
      packageLinks = {@Ord.PackageLink(type = "terms-of-use", url = "https://example.com/tou")})
  static class WithPackageLinks {}

  // ── generate — returns non-null entity ───────────────────────────────────

  @Test
  void givenAnnotation_whenGenerateIsCalled_thenEntityIsNotNull() {
    Ord.Vendor annotation = WithPlainFields.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithPlainFields.class, new DocumentSchema()));

    assertNotNull(result);
  }

  // ── processPlain — String → String ───────────────────────────────────────

  @Test
  void givenAnnotationWithStringFields_whenGenerateIsCalled_thenStringFieldsAreSet() {
    Ord.Vendor annotation = WithPlainFields.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithPlainFields.class, new DocumentSchema()));

    assertEquals("sap:vendor:SAP:v1", result.getOrdId());
    assertEquals("SAP SE", result.getTitle());
  }

  // ── processPlain — empty String falls back to tryGenerateDefault ──────────

  @Test
  void givenAnnotationWithEmptyStringFields_whenGenerateIsCalled_thenDefaultIsGenerated() {
    Ord.Vendor annotation = WithAllDefaults.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithAllDefaults.class, new DocumentSchema()));

    assertEquals(WithAllDefaults.class.getSimpleName(), result.getTitle());
  }

  // ── processPlain — String → URI conversion ───────────────────────────────

  @Test
  void givenAnnotationWithUrlField_whenGenerateIsCalled_thenUriFieldIsSet() {
    EntityAutoGenerator<Ord.Link, Link> generator = new EntityAutoGenerator<>(Link::new) {};
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    @Ord.Link(url = "https://example.com/api", title = "API Docs")
    class WithUrl {}

    Ord.Link annotation = WithUrl.class.getAnnotation(Ord.Link.class);

    Link result = generator.generate(Context.of(annotation, WithUrl.class, new DocumentSchema()));

    assertEquals(URI.create("https://example.com/api"), result.getUrl());
  }

  // ── processPlain — String → Date conversion ──────────────────────────────

  @Test
  void givenAnnotationWithDateField_whenGenerateIsCalled_thenDateFieldIsSet() {
    Ord.ApiResource annotation = WithDateField.class.getAnnotation(Ord.ApiResource.class);

    ApiResource result =
        apiResourceGenerator.generate(Context.of(annotation, WithDateField.class, new DocumentSchema()));

    Date lastUpdate = result.getLastUpdate();
    assertNotNull(lastUpdate);
    @SuppressWarnings("deprecation")
    int year = lastUpdate.getYear() + 1900;
    assertEquals(2024, year);
  }

  // ── processPlain — String → Supported (enum) conversion ──────────────────

  @Test
  void givenAnnotationWithSupportedField_whenGenerateIsCalled_thenSupportedEnumIsSet() {
    EntityAutoGenerator<Ord.Extensible, org.openresourcediscovery.model.Extensible> generator =
        new EntityAutoGenerator<>(org.openresourcediscovery.model.Extensible::new) {};
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    @Ord.Extensible(supported = "manual")
    class WithSupported {}

    Ord.Extensible annotation = WithSupported.class.getAnnotation(Ord.Extensible.class);

    org.openresourcediscovery.model.Extensible result =
        generator.generate(Context.of(annotation, WithSupported.class, new DocumentSchema()));

    assertEquals("manual", result.getSupported());
  }

  // ── processPlainArrays — String[] → List ─────────────────────────────────

  @Test
  void givenAnnotationWithStringArrayField_whenGenerateIsCalled_thenListFieldIsSet() {
    Ord.Vendor annotation = WithTags.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithTags.class, new DocumentSchema()));

    assertEquals(List.of("cloud", "enterprise"), result.getTags());
  }

  @Test
  void givenAnnotationWithAnotherStringArrayField_whenGenerateIsCalled_thenListFieldIsSet() {
    Ord.Vendor annotation = WithPartners.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithPartners.class, new DocumentSchema()));

    assertEquals(List.of("sap:vendor:Partner:v1"), result.getPartners());
  }

  @Test
  void givenAnnotationWithEmptyStringArrayField_whenGenerateIsCalled_thenListFieldIsNull() {
    Ord.Vendor annotation = WithAllDefaults.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithAllDefaults.class, new DocumentSchema()));

    assertNull(result.getTags());
  }

  // ── processAnnotations — non-empty sub-annotation mapped via sub-generator ─

  @Test
  void givenAnnotationWithNonEmptySubAnnotation_whenGenerateIsCalled_thenSubEntityIsSet() {
    LabelsGenerator labelsGenerator = new LabelsGenerator();
    labelsGenerator.setOrdProperties(ordProperties);
    labelsGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    doReturn(labelsGenerator).when(entityGeneratorFactory).create(Ord.Labels.class);

    Ord.Vendor annotation = WithLabels.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithLabels.class, new DocumentSchema()));

    Labels labels = result.getLabels();
    assertNotNull(labels);
    assertEquals(List.of("prod", "dev"), labels.getAdditionalProperties().get("env"));
  }

  // ── processAnnotations — empty sub-annotation is skipped ─────────────────

  @Test
  void givenAnnotationWithEmptySubAnnotation_whenGenerateIsCalled_thenSubEntityIsNull() {
    Ord.Vendor annotation = WithAllDefaults.class.getAnnotation(Ord.Vendor.class);

    Vendor result = vendorGenerator.generate(Context.of(annotation, WithAllDefaults.class, new DocumentSchema()));

    assertNull(result.getLabels());
  }

  // ── processAnnotationArrays — annotation[] mapped via sub-generator ───────

  @Test
  void givenAnnotationWithAnnotationArray_whenGenerateIsCalled_thenListOfSubEntitiesIsSet() {
    EntityAutoGenerator<Ord.Package, Package> packageGenerator = new EntityAutoGenerator<>(Package::new) {};
    packageGenerator.setOrdProperties(ordProperties);
    packageGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.PackageLink, PackageLink> packageLinkGenerator =
        new EntityAutoGenerator<>(PackageLink::new) {};
    packageLinkGenerator.setOrdProperties(ordProperties);
    packageLinkGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.Link, Link> linkGenerator = new EntityAutoGenerator<>(Link::new) {};
    linkGenerator.setOrdProperties(ordProperties);
    linkGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.File, File> fileGenerator = new EntityAutoGenerator<>(File::new) {};
    fileGenerator.setOrdProperties(ordProperties);
    fileGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    doReturn(packageLinkGenerator).when(entityGeneratorFactory).create(Ord.PackageLink.class);
    doReturn(linkGenerator).when(entityGeneratorFactory).create(Ord.Link.class);
    doReturn(fileGenerator).when(entityGeneratorFactory).create(Ord.File.class);

    Ord.Package annotation = WithPackageLinks.class.getAnnotation(Ord.Package.class);

    Package result =
        packageGenerator.generate(Context.of(annotation, WithPackageLinks.class, new DocumentSchema()));

    List<PackageLink> links = result.getPackageLinks();
    assertNotNull(links);
    assertEquals(1, links.size());
    assertEquals("terms-of-use", links.get(0).getType());
    assertEquals(URI.create("https://example.com/tou"), links.get(0).getUrl());
  }

  // ── processAnnotationArrays — empty annotation[] → null ──────────────────

  @Test
  void givenAnnotationWithEmptyAnnotationArray_whenGenerateIsCalled_thenListFieldIsNull() {
    EntityAutoGenerator<Ord.Package, Package> packageGenerator = new EntityAutoGenerator<>(Package::new) {};
    packageGenerator.setOrdProperties(ordProperties);
    packageGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.PackageLink, PackageLink> packageLinkGenerator =
        new EntityAutoGenerator<>(PackageLink::new) {};
    packageLinkGenerator.setOrdProperties(ordProperties);
    packageLinkGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.Link, Link> linkGenerator = new EntityAutoGenerator<>(Link::new) {};
    linkGenerator.setOrdProperties(ordProperties);
    linkGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    EntityAutoGenerator<Ord.File, File> fileGenerator = new EntityAutoGenerator<>(File::new) {};
    fileGenerator.setOrdProperties(ordProperties);
    fileGenerator.setEntityGeneratorFactory(entityGeneratorFactory);

    doReturn(packageLinkGenerator).when(entityGeneratorFactory).create(Ord.PackageLink.class);
    doReturn(linkGenerator).when(entityGeneratorFactory).create(Ord.Link.class);
    doReturn(fileGenerator).when(entityGeneratorFactory).create(Ord.File.class);

    @Ord.Package(
        ordId = "customer:package:TestPackage:v1",
        title = "Test Package",
        shortDescription = "A short description",
        description = "A description",
        version = "1.0.0",
        vendor = "customer:vendor:Customer:v1")
    class WithEmptyPackage {}

    Ord.Package annotation = WithEmptyPackage.class.getAnnotation(Ord.Package.class);

    Package result =
        packageGenerator.generate(Context.of(annotation, WithEmptyPackage.class, new DocumentSchema()));

    assertNull(result.getPackageLinks());
    assertNull(result.getLinks());
  }
}
