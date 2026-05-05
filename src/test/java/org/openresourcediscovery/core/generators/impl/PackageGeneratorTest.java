package org.openresourcediscovery.core.generators.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.lang.annotation.Annotation;
import java.net.URI;
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
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.DocumentationLabels;
import org.openresourcediscovery.model.File;
import org.openresourcediscovery.model.Labels;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.Package;
import org.openresourcediscovery.model.PackageLink;
import org.openresourcediscovery.testutils.Annotations;

@ExtendWith(MockitoExtension.class)
class PackageGeneratorTest {

  private static final String NAMESPACE = "customer.test.namespace";

  @Mock
  private OrdProperties ordProperties;

  @Mock
  private EntityGeneratorFactory entityGeneratorFactory;

  private PackageGenerator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new PackageGenerator();

    classUnderTest.setOrdProperties(ordProperties);
    classUnderTest.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(NAMESPACE).when(ordProperties).getNamespace();
    prepareEntityGeneratorFactoryMock(Ord.Labels.class, new LabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.Link.class, new EntityAutoGenerator<>(Link::new));
    prepareEntityGeneratorFactoryMock(Ord.File.class, new EntityAutoGenerator<>(File::new));
    prepareEntityGeneratorFactoryMock(Ord.DocumentationLabels.class, new DocumentationLabelsGenerator());
    prepareEntityGeneratorFactoryMock(Ord.PackageLink.class, new EntityAutoGenerator<>(PackageLink::new));
  }

  @Test
  public void verifyAnnotationPropertiesCount() {
    assertEquals(26, Ord.Package.class.getDeclaredMethods().length);
  }

  @Test
  void givenNoAnnotationValues_whenGenerateIsCalled_thenDefaultsAreApplied() {
    assertEquals(
        new Package()
            .withVersion("1.0.0")
            .withPolicyLevel("none")
            .withTitle(getClass().getSimpleName())
            .withVendor("customer:vendor:Customer:")
            .withOrdId(NAMESPACE + ":package:" + getClass().getSimpleName() + ":v1")
            .withDescription(
                "Auto-generated description for " + getClass().getSimpleName())
            .withShortDescription("Auto-generated short description for "
                + getClass().getSimpleName()),
        classUnderTest.generate(EntityGenerator.Context.of(
            Annotations.mock(Ord.Package.class), getClass(), new DocumentSchema())));
  }

  @Test
  void whenGenerateIsCalled_thenCorrectResultIsReturned() {
    Ord.Package annotation = Annotations.mock(
        Ord.Package.class,
        Map.ofEntries(
            Map.entry("version", "2.0.0"),
            Map.entry("title", "Test Package"),
            Map.entry("vendor", "test-vendor"),
            Map.entry("localId", "test-local-id"),
            Map.entry("licenseType", "Apache 2.0"),
            Map.entry("ordId", "test:package:Custom:v1"),
            Map.entry("description", "Test description"),
            Map.entry("supportInfo", "test-support-info"),
            Map.entry("policyLevel", "test-policy-level"),
            Map.entry("countries", new String[] {"DE", "US"}),
            Map.entry("labels", createLabelsAnnotationMock()),
            Map.entry("tags", new String[] {"tag-1", "tag-2"}),
            Map.entry("shortDescription", "Test short description"),
            Map.entry("correlationIds", new String[] {"cid-1", "cid-2"}),
            Map.entry("customPolicyLevel", "test-custom-policy-level"),
            Map.entry("runtimeRestriction", "test-runtime-restriction"),
            Map.entry("links", new Ord.Link[] {createLinkAnnotationMock()}),
            Map.entry("lineOfBusiness", new String[] {"test-lob-1", "test-lob-2"}),
            Map.entry("industry", new String[] {"test-industry-1", "test-industry-2"}),
            Map.entry("documentationLabels", createDocumentationLabelsAnnotationMock()),
            Map.entry("partOfProducts", new String[] {"test-product-1", "test-product-2"}),
            Map.entry("packageLinks", new Ord.PackageLink[] {createPackageLinkAnnotationMock()}),
            Map.entry("policyLevels", new String[] {"test-policy-level-1", "test-policy-level-2"}),
            Map.entry("files", new Ord.File[] {createFileAnnotationMock()})));

    assertEquals(
        new Package()
            .withVersion("2.0.0")
            .withTitle("Test Package")
            .withVendor("test-vendor")
            .withLocalId("test-local-id")
            .withLicenseType("Apache 2.0")
            .withCountries(List.of("DE", "US"))
            .withOrdId("test:package:Custom:v1")
            .withDescription("Test description")
            .withTags(List.of("tag-1", "tag-2"))
            .withSupportInfo("test-support-info")
            .withPolicyLevel("test-policy-level")
            .withCorrelationIds(List.of("cid-1", "cid-2"))
            .withShortDescription("Test short description")
            .withCustomPolicyLevel("test-custom-policy-level")
            .withRuntimeRestriction("test-runtime-restriction")
            .withLineOfBusiness(List.of("test-lob-1", "test-lob-2"))
            .withIndustry(List.of("test-industry-1", "test-industry-2"))
            .withPartOfProducts(List.of("test-product-1", "test-product-2"))
            .withPolicyLevels(List.of("test-policy-level-1", "test-policy-level-2"))
            .withLabels(new Labels()
                .withAdditionalProperty(
                    "test-label-key", List.of("test-label-value-1", "test-label-value-2")))
            .withLinks(List.of(new Link()
                .withTitle("test-link-title")
                .withDescription("test-link-description")
                .withUrl(URI.create("https://test-link.dummy.nowhere.org"))))
            .withDocumentationLabels(new DocumentationLabels()
                .withAdditionalProperty(
                    "test-doc-label-key",
                    List.of("test-doc-label-value-1", "test-doc-label-value-2")))
            .withPackageLinks(List.of(new PackageLink()
                .withType("test-type")
                .withCustomType("test-custom-type")
                .withUrl(URI.create("https://test-package-link.dummy.nowhere.org"))))
            .withFiles(List.of(new File()
                .withTitle("test-file-title")
                .withUrl("https://test-file.dummy.nowhere.org")
                .withDescription("test-file-description")
                .withMediaType("application/pdf"))),
        classUnderTest.generate(EntityGenerator.Context.of(annotation, getClass(), new DocumentSchema())));
  }

  private <T extends Annotation, E> void prepareEntityGeneratorFactoryMock(
      Class<T> annotation, EntityGenerator<T, E> generator) {
    generator.setOrdProperties(ordProperties);
    generator.setEntityGeneratorFactory(entityGeneratorFactory);

    lenient().doReturn(generator).when(entityGeneratorFactory).create(annotation);
  }

  private static Ord.Link createLinkAnnotationMock() {
    return Annotations.mock(
        Ord.Link.class,
        Map.ofEntries(
            Map.entry("title", "test-link-title"),
            Map.entry("description", "test-link-description"),
            Map.entry("url", "https://test-link.dummy.nowhere.org")));
  }

  private static Ord.Labels createLabelsAnnotationMock() {
    return Annotations.mock(Ord.Labels.class, Map.of("value", new Ord.LabelsEntry[] {
      Annotations.mock(
          Ord.LabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-label-key"),
              Map.entry("values", new String[] {"test-label-value-1", "test-label-value-2"})))
    }));
  }

  private static Ord.PackageLink createPackageLinkAnnotationMock() {
    return Annotations.mock(
        Ord.PackageLink.class,
        Map.ofEntries(
            Map.entry("type", "test-type"),
            Map.entry("customType", "test-custom-type"),
            Map.entry("url", "https://test-package-link.dummy.nowhere.org")));
  }

  private static Ord.DocumentationLabels createDocumentationLabelsAnnotationMock() {
    return Annotations.mock(Ord.DocumentationLabels.class, Map.of("value", new Ord.DocumentationLabelsEntry[] {
      Annotations.mock(
          Ord.DocumentationLabelsEntry.class,
          Map.ofEntries(
              Map.entry("key", "test-doc-label-key"),
              Map.entry("values", new String[] {"test-doc-label-value-1", "test-doc-label-value-2"})))
    }));
  }

  private static Ord.File createFileAnnotationMock() {
    return Annotations.mock(
        Ord.File.class,
        Map.ofEntries(
            Map.entry("title", "test-file-title"),
            Map.entry("url", "https://test-file.dummy.nowhere.org"),
            Map.entry("description", "test-file-description"),
            Map.entry("mediaType", "application/pdf")));
  }
}
