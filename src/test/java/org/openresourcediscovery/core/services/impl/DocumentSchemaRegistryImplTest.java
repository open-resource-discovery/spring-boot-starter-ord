package org.openresourcediscovery.core.services.impl;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openresourcediscovery.model.Agent;
import org.openresourcediscovery.model.ApiResource;
import org.openresourcediscovery.model.ApiResourceDefinition;
import org.openresourcediscovery.model.Capability;
import org.openresourcediscovery.model.CapabilityDefinition;
import org.openresourcediscovery.model.ConsumptionBundle;
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.EntityType;
import org.openresourcediscovery.model.EventResource;
import org.openresourcediscovery.model.EventResourceDefinition;
import org.openresourcediscovery.model.IntegrationDependency;
import org.openresourcediscovery.model.Overlay;
import org.openresourcediscovery.model.Package;

class DocumentSchemaRegistryImplTest {

  private static final String DOC_NAME = "doc-1";
  private static final String PUBLIC = "public";
  private static final String INTERNAL = "internal";
  private static final String PACKAGE_ORD_ID = "customer:package:test:v1";

  private DocumentSchemaRegistryImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new DocumentSchemaRegistryImpl(new ObjectMapper());
  }

  // ── register / getAllDocumentIds ────────────────────────────────────────────

  @Test
  void givenNoDocuments_whenGetAllDocumentSchemasIsCalled_thenEmptySetIsReturned() {
    assertTrue(classUnderTest.getAllDocumentSchemas().isEmpty());
  }

  @Test
  void givenRegisteredDocument_whenGetAllDocumentSchemasIsCalled_thenIdIsReturned() {
    DocumentSchema doc1 = new DocumentSchema().withDescription("doc-1");

    classUnderTest.register("doc-1", Set.of(), doc1);

    assertEquals(ofEntries(entry("doc-1", doc1)), classUnderTest.getAllDocumentSchemas());
  }

  @Test
  void givenMultipleDocuments_whenGetAllDocumentSchemasIsCalled_thenAllNamesAreReturned() {
    DocumentSchema doc1 = new DocumentSchema().withDescription("doc-1");
    DocumentSchema doc2 = new DocumentSchema().withDescription("doc-2");

    classUnderTest.register("doc-1", Set.of(), doc1).register("doc-2", Set.of(), doc2);

    assertEquals(ofEntries(entry("doc-1", doc1), entry("doc-2", doc2)), classUnderTest.getAllDocumentSchemas());
  }

  // ── lookupAccessStrategies ──────────────────────────────────────────────────

  @Test
  void givenUnknownId_whenLookupAccessStrategiesIsCalled_thenEmptySetIsReturned() {
    assertTrue(classUnderTest.lookupAccessStrategies("unknown").isEmpty());
  }

  @Test
  void givenRegisteredDocument_whenLookupAccessStrategiesIsCalled_thenStrategiesAreReturned() {
    classUnderTest.register(DOC_NAME, Set.of("open", "sap:cmp-mtls:v1"), new DocumentSchema());

    assertEquals(Set.of("open", "sap:cmp-mtls:v1"), classUnderTest.lookupAccessStrategies(DOC_NAME));
  }

  // ── lookupDocumentSchema – missing document ─────────────────────────────────

  @Test
  void givenUnknownId_whenLookupDocumentSchemaIsCalled_thenEmptyOptionalIsReturned() {
    Optional<DocumentSchema> result = classUnderTest.lookupDocumentSchema("unknown", Set.of(PUBLIC));

    assertTrue(result.isEmpty());
  }

  // ── lookupDocumentSchema – document is cloned ──────────────────────────────

  @Test
  void givenRegisteredDocument_whenLookupDocumentSchemaIsCalled_thenCloneIsReturned() {
    DocumentSchema original = new DocumentSchema();
    classUnderTest.register(DOC_NAME, Set.of(), original);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertNotSame(original, result);
  }

  // ── filterAgents ───────────────────────────────────────────────────────────

  @Test
  void givenAgentsWithMatchingVisibility_whenLookupIsCalled_thenAgentsAreIncluded() {
    Agent agent = new Agent().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc = new DocumentSchema().withAgents(List.of(agent)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getAgents().size());
  }

  @Test
  void givenAgentsWithNonMatchingVisibility_whenLookupIsCalled_thenAgentsAreExcluded() {
    Agent agent = new Agent().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withAgents(List.of(agent));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getAgents().isEmpty());
  }

  // ── filterPackages ─────────────────────────────────────────────────────────

  @Test
  void givenPackageReferencedByVisibleApiResource_whenLookupIsCalled_thenPackageIsIncluded() {
    ApiResource api = new ApiResource().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getPackages().size());
  }

  @Test
  void givenPackageOnlyReferencedByInvisibleResources_whenLookupIsCalled_thenPackageIsExcluded() {
    ApiResource api = new ApiResource().withVisibility(INTERNAL).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getPackages().isEmpty());
  }

  @Test
  void givenPackageReferencedByVisibleDataProduct_whenLookupIsCalled_thenPackageIsIncluded() {
    DataProduct dp = new DataProduct().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc = new DocumentSchema().withDataProducts(List.of(dp)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getPackages().size());
  }

  @Test
  void givenPackageReferencedByVisibleEventResource_whenLookupIsCalled_thenPackageIsIncluded() {
    EventResource er = new EventResource().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc =
        new DocumentSchema().withEventResources(List.of(er)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getPackages().size());
  }

  @Test
  void givenPackageReferencedByVisibleIntegrationDependency_whenLookupIsCalled_thenPackageIsIncluded() {
    IntegrationDependency id =
        new IntegrationDependency().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc =
        new DocumentSchema().withIntegrationDependencies(List.of(id)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getPackages().size());
  }

  // ── filterEntityTypes ──────────────────────────────────────────────────────

  @Test
  void givenEntityTypeWithMatchingVisibility_whenLookupIsCalled_thenEntityTypeIsIncluded() {
    EntityType et = new EntityType().withVisibility(PUBLIC).withPartOfPackage(PACKAGE_ORD_ID);
    Package pkg = new Package().withOrdId(PACKAGE_ORD_ID);
    DocumentSchema doc = new DocumentSchema().withEntityTypes(List.of(et)).withPackages(List.of(pkg));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getEntityTypes().size());
  }

  @Test
  void givenEntityTypeWithNonMatchingVisibility_whenLookupIsCalled_thenEntityTypeIsExcluded() {
    EntityType et = new EntityType().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withEntityTypes(List.of(et));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getEntityTypes().isEmpty());
  }

  // ── filterApiResources ─────────────────────────────────────────────────────

  @Test
  void givenApiResourceWithMatchingVisibility_whenLookupIsCalled_thenApiResourceIsIncluded() {
    ApiResource api = new ApiResource().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getApiResources().size());
  }

  @Test
  void givenApiResourceWithNonMatchingVisibility_whenLookupIsCalled_thenApiResourceIsExcluded() {
    ApiResource api = new ApiResource().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getApiResources().isEmpty());
  }

  @Test
  void givenApiResourceDefinitionInheritingVisibility_whenLookupIsCalled_thenDefinitionIsIncluded() {
    ApiResourceDefinition def = new ApiResourceDefinition(); // no own visibility → inherits
    ApiResource api = new ApiResource().withVisibility(PUBLIC).withResourceDefinitions(List.of(def));
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getApiResources().get(0).getResourceDefinitions().size());
  }

  @Test
  void givenApiResourceDefinitionWithOwnNonMatchingVisibility_whenLookupIsCalled_thenDefinitionIsExcluded() {
    ApiResourceDefinition def = new ApiResourceDefinition().withVisibility(INTERNAL);
    ApiResource api = new ApiResource().withVisibility(PUBLIC).withResourceDefinitions(List.of(def));
    DocumentSchema doc = new DocumentSchema().withApiResources(List.of(api));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getApiResources().get(0).getResourceDefinitions().isEmpty());
  }

  // ── filterCapabilities ─────────────────────────────────────────────────────

  @Test
  void givenCapabilityWithMatchingVisibility_whenLookupIsCalled_thenCapabilityIsIncluded() {
    Capability cap = new Capability().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withCapabilities(List.of(cap));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getCapabilities().size());
  }

  @Test
  void givenCapabilityDefinitionInheritingVisibility_whenLookupIsCalled_thenDefinitionIsIncluded() {
    CapabilityDefinition def = new CapabilityDefinition(); // no own visibility → inherits
    Capability cap = new Capability().withVisibility(PUBLIC).withDefinitions(List.of(def));
    DocumentSchema doc = new DocumentSchema().withCapabilities(List.of(cap));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getCapabilities().get(0).getDefinitions().size());
  }

  // ── filterDataProducts ─────────────────────────────────────────────────────

  @Test
  void givenDataProductWithMatchingVisibility_whenLookupIsCalled_thenDataProductIsIncluded() {
    DataProduct dp = new DataProduct().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withDataProducts(List.of(dp));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getDataProducts().size());
  }

  @Test
  void givenDataProductWithNonMatchingVisibility_whenLookupIsCalled_thenDataProductIsExcluded() {
    DataProduct dp = new DataProduct().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withDataProducts(List.of(dp));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getDataProducts().isEmpty());
  }

  // ── filterEventResources ───────────────────────────────────────────────────

  @Test
  void givenEventResourceWithMatchingVisibility_whenLookupIsCalled_thenEventResourceIsIncluded() {
    EventResource er = new EventResource().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withEventResources(List.of(er));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getEventResources().size());
  }

  @Test
  void givenEventResourceDefinitionInheritingVisibility_whenLookupIsCalled_thenDefinitionIsIncluded() {
    EventResourceDefinition def = new EventResourceDefinition(); // no own visibility → inherits
    EventResource er = new EventResource().withVisibility(PUBLIC).withResourceDefinitions(List.of(def));
    DocumentSchema doc = new DocumentSchema().withEventResources(List.of(er));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(
        1, result.getEventResources().get(0).getResourceDefinitions().size());
  }

  // ── filterConsumptionBundles ───────────────────────────────────────────────

  @Test
  void givenConsumptionBundleWithNullVisibility_whenLookupIsCalled_thenBundleIsIncluded() {
    ConsumptionBundle bundle = new ConsumptionBundle(); // visibility is null
    DocumentSchema doc = new DocumentSchema().withConsumptionBundles(List.of(bundle));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getConsumptionBundles().size());
  }

  @Test
  void givenConsumptionBundleWithMatchingVisibility_whenLookupIsCalled_thenBundleIsIncluded() {
    ConsumptionBundle bundle = new ConsumptionBundle().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withConsumptionBundles(List.of(bundle));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getConsumptionBundles().size());
  }

  @Test
  void givenConsumptionBundleWithNonMatchingVisibility_whenLookupIsCalled_thenBundleIsExcluded() {
    ConsumptionBundle bundle = new ConsumptionBundle().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withConsumptionBundles(List.of(bundle));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getConsumptionBundles().isEmpty());
  }

  // ── filterIntegrationDependencies ──────────────────────────────────────────

  @Test
  void givenIntegrationDependencyWithMatchingVisibility_whenLookupIsCalled_thenItIsIncluded() {
    IntegrationDependency dep = new IntegrationDependency().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withIntegrationDependencies(List.of(dep));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getIntegrationDependencies().size());
  }

  @Test
  void givenIntegrationDependencyWithNonMatchingVisibility_whenLookupIsCalled_thenItIsExcluded() {
    IntegrationDependency dep = new IntegrationDependency().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withIntegrationDependencies(List.of(dep));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getIntegrationDependencies().isEmpty());
  }

  // ── filterOverlays ──────────────────────────────────────────

  @Test
  void givenOverlayWithMatchingVisibility_whenLookupIsCalled_thenItIsIncluded() {
    Overlay dep = new Overlay().withVisibility(PUBLIC);
    DocumentSchema doc = new DocumentSchema().withOverlays(List.of(dep));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertEquals(1, result.getOverlays().size());
  }

  @Test
  void givenOverlayWithNonMatchingVisibility_whenLookupIsCalled_thenItIsExcluded() {
    Overlay dep = new Overlay().withVisibility(INTERNAL);
    DocumentSchema doc = new DocumentSchema().withOverlays(List.of(dep));
    classUnderTest.register(DOC_NAME, Set.of(), doc);

    DocumentSchema result =
        classUnderTest.lookupDocumentSchema(DOC_NAME, Set.of(PUBLIC)).orElseThrow();

    assertTrue(result.getOverlays().isEmpty());
  }
}
