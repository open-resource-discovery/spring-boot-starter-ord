package org.openresourcediscovery.core.services.impl;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.firstNonEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
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

public class DocumentSchemaRegistryImpl implements DocumentSchemaRegistry {

  private final ObjectMapper objectMapper;
  private final Map<String, DocumentSchema> documents;
  private final Map<String, Set<String>> accessStrategies;

  public DocumentSchemaRegistryImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.documents = new ConcurrentHashMap<>();
    this.accessStrategies = new ConcurrentHashMap<>();
  }

  public DocumentSchemaRegistryImpl register(String name, Set<String> strategies, DocumentSchema document) {
    documents.put(name, document);
    accessStrategies.put(name, strategies);

    return this;
  }

  @Override
  public Map<String, DocumentSchema> getAllDocumentSchemas() {
    return Map.copyOf(documents);
  }

  @Override
  public Set<String> lookupAccessStrategies(String name) {
    return unmodifiableSet(accessStrategies.getOrDefault(name, emptySet()));
  }

  @Override
  public Optional<DocumentSchema> lookupDocumentSchema(String name, Set<String> visibilities) {
    return Optional.ofNullable(documents.get(name))
        // Clone the document to avoid mutating the original when applying visibility filters
        .map(d -> objectMapper.convertValue(d, DocumentSchema.class))
        // Apply visibility filters to all components of the document
        .map(d -> d.withAgents(filterAgents(d, visibilities)))
        .map(d -> d.withPackages(filterPackages(d, visibilities)))
        .map(d -> d.withEntityTypes(filterEntityTypes(d, visibilities)))
        .map(d -> d.withApiResources(filterApiResources(d, visibilities)))
        .map(d -> d.withCapabilities(filterCapabilities(d, visibilities)))
        .map(d -> d.withDataProducts(filterDataProducts(d, visibilities)))
        .map(d -> d.withEventResources(filterEventResources(d, visibilities)))
        .map(d -> d.withConsumptionBundles(filterConsumptionBundles(d, visibilities)))
        .map(d -> d.withOverlays(filterOverlays(d, visibilities)))
        .map(d -> d.withIntegrationDependencies(filterIntegrationDependencies(d, visibilities)));
  }

  private static List<Agent> filterAgents(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getAgents()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<Overlay> filterOverlays(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getOverlays()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<Package> filterPackages(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getPackages()).stream()
        .filter(p -> emptyIfNull(document.getAgents()).stream()
                .filter(a -> visibilities.contains(a.getVisibility()))
                .anyMatch(a -> Objects.equals(p.getOrdId(), a.getPartOfPackage()))
            || emptyIfNull(document.getEntityTypes()).stream()
                .filter(et -> visibilities.contains(et.getVisibility()))
                .anyMatch(et -> Objects.equals(p.getOrdId(), et.getPartOfPackage()))
            || emptyIfNull(document.getCapabilities()).stream()
                .filter(c -> visibilities.contains(c.getVisibility()))
                .anyMatch(c -> Objects.equals(p.getOrdId(), c.getPartOfPackage()))
            || emptyIfNull(document.getApiResources()).stream()
                .filter(ar -> visibilities.contains(ar.getVisibility()))
                .anyMatch(ar -> Objects.equals(p.getOrdId(), ar.getPartOfPackage()))
            || emptyIfNull(document.getDataProducts()).stream()
                .filter(dp -> visibilities.contains(dp.getVisibility()))
                .anyMatch(dp -> Objects.equals(p.getOrdId(), dp.getPartOfPackage()))
            || emptyIfNull(document.getEventResources()).stream()
                .filter(er -> visibilities.contains(er.getVisibility()))
                .anyMatch(er -> Objects.equals(p.getOrdId(), er.getPartOfPackage()))
            || emptyIfNull(document.getIntegrationDependencies()).stream()
                .filter(id -> visibilities.contains(id.getVisibility()))
                .anyMatch(id -> Objects.equals(p.getOrdId(), id.getPartOfPackage())))
        .toList();
  }

  private static List<EntityType> filterEntityTypes(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getEntityTypes()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<Capability> filterCapabilities(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getCapabilities()).stream()
        .filter(c -> visibilities.contains(c.getVisibility()))
        .map(c -> c.withDefinitions(filterCapabilityDefinitions(c, visibilities)))
        .toList();
  }

  private static List<DataProduct> filterDataProducts(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getDataProducts()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<ApiResource> filterApiResources(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getApiResources()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .map(r -> r.withResourceDefinitions(filterApiResourceDefinitions(r, visibilities)))
        .toList();
  }

  private static List<EventResource> filterEventResources(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getEventResources()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .map(r -> r.withResourceDefinitions(filterEventResourceDefinitions(r, visibilities)))
        .toList();
  }

  private static List<ConsumptionBundle> filterConsumptionBundles(DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getConsumptionBundles()).stream()
        .filter(r -> isNull(r.getVisibility()) || visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<CapabilityDefinition> filterCapabilityDefinitions(
      Capability capability, Set<String> visibilities) {
    return emptyIfNull(capability.getDefinitions()).stream()
        .filter(r -> visibilities.contains(firstNonEmpty(r.getVisibility(), capability.getVisibility())))
        .toList();
  }

  private static List<ApiResourceDefinition> filterApiResourceDefinitions(
      ApiResource resource, Set<String> visibilities) {
    return emptyIfNull(resource.getResourceDefinitions()).stream()
        .filter(r -> visibilities.contains(firstNonEmpty(r.getVisibility(), resource.getVisibility())))
        .toList();
  }

  private static List<IntegrationDependency> filterIntegrationDependencies(
      DocumentSchema document, Set<String> visibilities) {
    return emptyIfNull(document.getIntegrationDependencies()).stream()
        .filter(r -> visibilities.contains(r.getVisibility()))
        .toList();
  }

  private static List<EventResourceDefinition> filterEventResourceDefinitions(
      EventResource resource, Set<String> visibilities) {
    return emptyIfNull(resource.getResourceDefinitions()).stream()
        .filter(r -> visibilities.contains(firstNonEmpty(r.getVisibility(), resource.getVisibility())))
        .toList();
  }
}
