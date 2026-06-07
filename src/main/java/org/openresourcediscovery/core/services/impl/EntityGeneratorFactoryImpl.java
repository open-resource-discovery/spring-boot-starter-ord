package org.openresourcediscovery.core.services.impl;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.impl.AgentGenerator;
import org.openresourcediscovery.core.generators.impl.ApiModelSelectorODataGenerator;
import org.openresourcediscovery.core.generators.impl.ApiResourceGenerator;
import org.openresourcediscovery.core.generators.impl.CapabilityGenerator;
import org.openresourcediscovery.core.generators.impl.ConsumptionBundleGenerator;
import org.openresourcediscovery.core.generators.impl.DataProductGenerator;
import org.openresourcediscovery.core.generators.impl.DocumentSchemaGenerator;
import org.openresourcediscovery.core.generators.impl.DocumentationLabelsGenerator;
import org.openresourcediscovery.core.generators.impl.EntityTypeGenerator;
import org.openresourcediscovery.core.generators.impl.EntityTypeOrdIdTargetGenerator;
import org.openresourcediscovery.core.generators.impl.EventResourceGenerator;
import org.openresourcediscovery.core.generators.impl.ExtensibleGenerator;
import org.openresourcediscovery.core.generators.impl.GroupGenerator;
import org.openresourcediscovery.core.generators.impl.GroupTypeGenerator;
import org.openresourcediscovery.core.generators.impl.IntegrationAspectGenerator;
import org.openresourcediscovery.core.generators.impl.IntegrationDependencyGenerator;
import org.openresourcediscovery.core.generators.impl.LabelsGenerator;
import org.openresourcediscovery.core.generators.impl.OverlayGenerator;
import org.openresourcediscovery.core.generators.impl.PackageGenerator;
import org.openresourcediscovery.core.generators.impl.ProductGenerator;
import org.openresourcediscovery.core.generators.impl.SystemInstanceGenerator;
import org.openresourcediscovery.core.generators.impl.SystemTypeGenerator;
import org.openresourcediscovery.core.generators.impl.SystemVersionGenerator;
import org.openresourcediscovery.core.generators.impl.VendorGenerator;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.APIEventResourceLink;
import org.openresourcediscovery.model.AccessStrategy;
import org.openresourcediscovery.model.ApiCompatibility;
import org.openresourcediscovery.model.ApiResourceDefinition;
import org.openresourcediscovery.model.ApiResourceIntegrationAspect;
import org.openresourcediscovery.model.ApiResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.CapabilityDefinition;
import org.openresourcediscovery.model.CapabilityIntegrationAspect;
import org.openresourcediscovery.model.ChangelogEntry;
import org.openresourcediscovery.model.ConsumptionBundleReference;
import org.openresourcediscovery.model.CredentialExchangeStrategy;
import org.openresourcediscovery.model.DataProductInputPort;
import org.openresourcediscovery.model.DataProductLink;
import org.openresourcediscovery.model.DataProductOutputPort;
import org.openresourcediscovery.model.EntityTypeDefinition;
import org.openresourcediscovery.model.EntityTypeMapping;
import org.openresourcediscovery.model.EventCompatibility;
import org.openresourcediscovery.model.EventResourceDefinition;
import org.openresourcediscovery.model.EventResourceIntegrationAspect;
import org.openresourcediscovery.model.EventResourceIntegrationAspectSubset;
import org.openresourcediscovery.model.ExposedApiResourcesTarget;
import org.openresourcediscovery.model.ExposedEntityType;
import org.openresourcediscovery.model.File;
import org.openresourcediscovery.model.Link;
import org.openresourcediscovery.model.OverlayDefinition;
import org.openresourcediscovery.model.PackageLink;
import org.openresourcediscovery.model.RelatedApiResource;
import org.openresourcediscovery.model.RelatedCapability;
import org.openresourcediscovery.model.RelatedEntityType;
import org.openresourcediscovery.model.RelatedEventResource;
import org.openresourcediscovery.model.Tombstone;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class EntityGeneratorFactoryImpl implements EntityGeneratorFactory {

  public static EntityGeneratorFactoryBuilder builder(AutowireCapableBeanFactory beanFactory) {
    return new EntityGeneratorFactoryBuilder() {

      private AutowireCapableBeanFactory autowireCapableBeanFactory = beanFactory;
      private Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>> suppliers =
          new HashMap<>(DEFAULT_SUPPLIERS);

      @Override
      public EntityGeneratorFactoryBuilder withAutowireCapableBeanFactory(
          AutowireCapableBeanFactory autowireCapableBeanFactory) {
        this.autowireCapableBeanFactory = autowireCapableBeanFactory;
        return this;
      }

      @Override
      public EntityGeneratorFactoryBuilder withSupplier(
          Class<? extends Annotation> annotation,
          Supplier<EntityGenerator<? extends Annotation, ?>> supplier) {
        this.suppliers.put(annotation, supplier);
        return this;
      }

      @Override
      public EntityGeneratorFactoryBuilder withSuppliers(
          Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>> suppliers) {
        this.suppliers = suppliers;
        return this;
      }

      @Override
      public EntityGeneratorFactory build() {
        return new EntityGeneratorFactoryImpl(this.autowireCapableBeanFactory, suppliers);
      }
    };
  }

  private static final Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>>
      DEFAULT_SUPPLIERS =
          Map.<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>>ofEntries(
              entry(
                  Ord.AccessStrategy.class,
                  () -> new EntityAutoGenerator<Ord.AccessStrategy, AccessStrategy>(
                      AccessStrategy::new)),
              entry(Ord.Agent.class, AgentGenerator::new),
              entry(
                  Ord.ApiCompatibility.class,
                  () -> new EntityAutoGenerator<Ord.ApiCompatibility, ApiCompatibility>(
                      ApiCompatibility::new)),
              entry(
                  Ord.APIEventResourceLink.class,
                  () -> new EntityAutoGenerator<Ord.APIEventResourceLink, APIEventResourceLink>(
                      APIEventResourceLink::new)),
              entry(Ord.ApiModelSelectorOData.class, ApiModelSelectorODataGenerator::new),
              entry(
                  Ord.ApiResourceDefinition.class,
                  () -> new EntityAutoGenerator<Ord.ApiResourceDefinition, ApiResourceDefinition>(
                      ApiResourceDefinition::new)),
              entry(Ord.ApiResource.class, ApiResourceGenerator::new),
              entry(
                  Ord.ApiResourceIntegrationAspect.class,
                  () -> new EntityAutoGenerator<
                      Ord.ApiResourceIntegrationAspect, ApiResourceIntegrationAspect>(
                      ApiResourceIntegrationAspect::new)),
              entry(
                  Ord.ApiResourceIntegrationAspectSubset.class,
                  () -> new EntityAutoGenerator<
                      Ord.ApiResourceIntegrationAspectSubset, ApiResourceIntegrationAspectSubset>(
                      ApiResourceIntegrationAspectSubset::new)),
              entry(
                  Ord.CapabilityDefinition.class,
                  () -> new EntityAutoGenerator<Ord.CapabilityDefinition, CapabilityDefinition>(
                      CapabilityDefinition::new)),
              entry(Ord.Capability.class, CapabilityGenerator::new),
              entry(
                  Ord.CapabilityIntegrationAspect.class,
                  () -> new EntityAutoGenerator<
                      Ord.CapabilityIntegrationAspect, CapabilityIntegrationAspect>(
                      CapabilityIntegrationAspect::new)),
              entry(
                  Ord.ChangelogEntry.class,
                  () -> new EntityAutoGenerator<Ord.ChangelogEntry, ChangelogEntry>(
                      ChangelogEntry::new)),
              entry(Ord.ConsumptionBundle.class, ConsumptionBundleGenerator::new),
              entry(
                  Ord.ConsumptionBundleReference.class,
                  () -> new EntityAutoGenerator<
                      Ord.ConsumptionBundleReference, ConsumptionBundleReference>(
                      ConsumptionBundleReference::new)),
              entry(
                  Ord.CredentialExchangeStrategy.class,
                  () -> new EntityAutoGenerator<
                      Ord.CredentialExchangeStrategy, CredentialExchangeStrategy>(
                      CredentialExchangeStrategy::new)),
              entry(Ord.DataProduct.class, DataProductGenerator::new),
              entry(
                  Ord.DataProductInputPort.class,
                  () -> new EntityAutoGenerator<Ord.DataProductInputPort, DataProductInputPort>(
                      DataProductInputPort::new)),
              entry(
                  Ord.DataProductLink.class,
                  () -> new EntityAutoGenerator<Ord.DataProductLink, DataProductLink>(
                      DataProductLink::new)),
              entry(
                  Ord.DataProductOutputPort.class,
                  () -> new EntityAutoGenerator<Ord.DataProductOutputPort, DataProductOutputPort>(
                      DataProductOutputPort::new)),
              entry(Ord.DocumentationLabels.class, DocumentationLabelsGenerator::new),
              entry(Ord.Document.class, DocumentSchemaGenerator::new),
              entry(Ord.EntityType.class, EntityTypeGenerator::new),
              entry(
                  Ord.EntityTypeDefinition.class,
                  () -> new EntityAutoGenerator<Ord.EntityTypeDefinition, EntityTypeDefinition>(
                      EntityTypeDefinition::new)),
              entry(
                  Ord.EntityTypeMapping.class,
                  () -> new EntityAutoGenerator<Ord.EntityTypeMapping, EntityTypeMapping>(
                      EntityTypeMapping::new)),
              entry(Ord.EntityTypeOrdIdTarget.class, EntityTypeOrdIdTargetGenerator::new),
              entry(
                  Ord.EventCompatibility.class,
                  () -> new EntityAutoGenerator<Ord.EventCompatibility, EventCompatibility>(
                      EventCompatibility::new)),
              entry(
                  Ord.EventResourceDefinition.class,
                  () -> new EntityAutoGenerator<Ord.EventResourceDefinition, EventResourceDefinition>(
                      EventResourceDefinition::new)),
              entry(Ord.EventResource.class, EventResourceGenerator::new),
              entry(
                  Ord.EventResourceIntegrationAspect.class,
                  () -> new EntityAutoGenerator<
                      Ord.EventResourceIntegrationAspect, EventResourceIntegrationAspect>(
                      EventResourceIntegrationAspect::new)),
              entry(
                  Ord.EventResourceIntegrationAspectSubset.class,
                  () -> new EntityAutoGenerator<
                      Ord.EventResourceIntegrationAspectSubset,
                      EventResourceIntegrationAspectSubset>(
                      EventResourceIntegrationAspectSubset::new)),
              entry(
                  Ord.ExposedApiResourcesTarget.class,
                  () -> new EntityAutoGenerator<
                      Ord.ExposedApiResourcesTarget, ExposedApiResourcesTarget>(
                      ExposedApiResourcesTarget::new)),
              entry(
                  Ord.ExposedEntityType.class,
                  () -> new EntityAutoGenerator<Ord.ExposedEntityType, ExposedEntityType>(
                      ExposedEntityType::new)),
              entry(Ord.Extensible.class, ExtensibleGenerator::new),
              entry(Ord.File.class, () -> new EntityAutoGenerator<Ord.File, File>(File::new)),
              entry(Ord.Group.class, GroupGenerator::new),
              entry(Ord.GroupType.class, GroupTypeGenerator::new),
              entry(Ord.IntegrationAspect.class, IntegrationAspectGenerator::new),
              entry(Ord.IntegrationDependency.class, IntegrationDependencyGenerator::new),
              entry(Ord.Labels.class, LabelsGenerator::new),
              entry(Ord.Link.class, () -> new EntityAutoGenerator<Ord.Link, Link>(Link::new)),
              entry(Ord.Overlay.class, OverlayGenerator::new),
              entry(
                  Ord.OverlayDefinition.class,
                  () -> new EntityAutoGenerator<Ord.OverlayDefinition, OverlayDefinition>(
                      OverlayDefinition::new)),
              entry(Ord.Package.class, PackageGenerator::new),
              entry(
                  Ord.PackageLink.class,
                  () -> new EntityAutoGenerator<Ord.PackageLink, PackageLink>(PackageLink::new)),
              entry(Ord.Product.class, ProductGenerator::new),
              entry(
                  Ord.RelatedApiResource.class,
                  () -> new EntityAutoGenerator<Ord.RelatedApiResource, RelatedApiResource>(
                      RelatedApiResource::new)),
              entry(
                  Ord.RelatedCapability.class,
                  () -> new EntityAutoGenerator<Ord.RelatedCapability, RelatedCapability>(
                      RelatedCapability::new)),
              entry(
                  Ord.RelatedEntityType.class,
                  () -> new EntityAutoGenerator<Ord.RelatedEntityType, RelatedEntityType>(
                      RelatedEntityType::new)),
              entry(
                  Ord.RelatedEventResource.class,
                  () -> new EntityAutoGenerator<Ord.RelatedEventResource, RelatedEventResource>(
                      RelatedEventResource::new)),
              entry(Ord.SystemInstance.class, SystemInstanceGenerator::new),
              entry(Ord.SystemType.class, SystemTypeGenerator::new),
              entry(Ord.SystemVersion.class, SystemVersionGenerator::new),
              entry(
                  Ord.Tombstone.class,
                  () -> new EntityAutoGenerator<Ord.Tombstone, Tombstone>(Tombstone::new)),
              entry(Ord.Vendor.class, VendorGenerator::new));

  private final AutowireCapableBeanFactory autowireCapableBeanFactory;
  private final Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>> suppliers;

  public EntityGeneratorFactoryImpl(
      AutowireCapableBeanFactory autowireCapableBeanFactory,
      Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>> suppliers) {
    this.suppliers = Map.copyOf(requireNonNull(suppliers));
    this.autowireCapableBeanFactory = requireNonNull(autowireCapableBeanFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends Annotation, T> EntityGenerator<A, T> create(Class<A> annotation) {
    return autowire(autowireCapableBeanFactory, (EntityGenerator<A, T>)
        requireNonNull(suppliers.get(annotation)).get());
  }

  private static <T> T autowire(AutowireCapableBeanFactory autowireCapableBeanFactory, T bean) {
    autowireCapableBeanFactory.autowireBeanProperties(bean, AUTOWIRE_BY_TYPE, true);

    return bean;
  }
}
