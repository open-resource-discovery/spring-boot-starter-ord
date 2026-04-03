package org.openresourcediscovery.core.services.impl;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Supplier;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.AgentAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.ApiResourceAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.CapabilityAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.ConsumptionBundleAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.DataProductAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.EntityTypeAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.EventResourceAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.GroupAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.GroupTypeAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.IntegrationDependencyAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.PackageAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.ProductAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemInstanceAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemTypeAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemVersionAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.TombstoneAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.VendorAnnotationProcessor;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

  private final AutowireCapableBeanFactory autowireCapableBeanFactory;
  private final Map<Class<? extends Annotation>, Supplier<AnnotationProcessor<? extends Annotation, ?>>> suppliers;

  public AnnotationProcessorFactoryImpl(AutowireCapableBeanFactory autowireCapableBeanFactory) {
    this.suppliers = instantiateSuppliers();
    this.autowireCapableBeanFactory = autowireCapableBeanFactory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends Annotation, T> AnnotationProcessor<A, T> create(Class<A> annotation) {
    return autowire(autowireCapableBeanFactory, (AnnotationProcessor<A, T>)
        requireNonNull(suppliers.get(annotation)).get());
  }

  private static <T> T autowire(AutowireCapableBeanFactory autowireCapableBeanFactory, T bean) {
    autowireCapableBeanFactory.autowireBeanProperties(bean, AUTOWIRE_BY_TYPE, true);

    return bean;
  }

  private static Map<Class<? extends Annotation>, Supplier<AnnotationProcessor<? extends Annotation, ?>>>
      instantiateSuppliers() {
    return Map.ofEntries(
        entry(Ord.Agent.class, AgentAnnotationProcessor::new),
        entry(Ord.ApiResource.class, ApiResourceAnnotationProcessor::new),
        entry(Ord.Capability.class, CapabilityAnnotationProcessor::new),
        entry(Ord.ConsumptionBundle.class, ConsumptionBundleAnnotationProcessor::new),
        entry(Ord.DataProduct.class, DataProductAnnotationProcessor::new),
        entry(Ord.EntityType.class, EntityTypeAnnotationProcessor::new),
        entry(Ord.EventResource.class, EventResourceAnnotationProcessor::new),
        entry(Ord.Group.class, GroupAnnotationProcessor::new),
        entry(Ord.GroupType.class, GroupTypeAnnotationProcessor::new),
        entry(Ord.IntegrationDependency.class, IntegrationDependencyAnnotationProcessor::new),
        entry(Ord.Package.class, PackageAnnotationProcessor::new),
        entry(Ord.Product.class, ProductAnnotationProcessor::new),
        entry(Ord.SystemInstance.class, SystemInstanceAnnotationProcessor::new),
        entry(Ord.SystemType.class, SystemTypeAnnotationProcessor::new),
        entry(Ord.SystemVersion.class, SystemVersionAnnotationProcessor::new),
        entry(Ord.Tombstone.class, TombstoneAnnotationProcessor::new),
        entry(Ord.Vendor.class, VendorAnnotationProcessor::new));
  }
}
