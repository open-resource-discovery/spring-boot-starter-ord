package org.openresourcediscovery.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.openresourcediscovery.core.services.impl.AnnotationProcessorFactoryImpl.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
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
import org.openresourcediscovery.core.processors.impl.OverlayAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.PackageAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.ProductAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemInstanceAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemTypeAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.SystemVersionAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.TombstoneAnnotationProcessor;
import org.openresourcediscovery.core.processors.impl.VendorAnnotationProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

@ExtendWith(MockitoExtension.class)
class AnnotationProcessorFactoryImplTest {

  @Mock
  private AutowireCapableBeanFactory autowireCapableBeanFactory;

  private AnnotationProcessorFactoryImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = (AnnotationProcessorFactoryImpl)
        builder(autowireCapableBeanFactory).build();
  }

  @Test
  void givenOverlayAnnotation_whenCreateIsCalled_thenOverlayAnnotationProcessorIsReturned() {
    assertInstanceOf(OverlayAnnotationProcessor.class, classUnderTest.create(Ord.Overlay.class));
  }

  @Test
  void givenAgentAnnotation_whenCreateIsCalled_thenAgentAnnotationProcessorIsReturned() {
    assertInstanceOf(AgentAnnotationProcessor.class, classUnderTest.create(Ord.Agent.class));
  }

  @Test
  void givenApiResourceAnnotation_whenCreateIsCalled_thenApiResourceAnnotationProcessorIsReturned() {
    assertInstanceOf(ApiResourceAnnotationProcessor.class, classUnderTest.create(Ord.ApiResource.class));
  }

  @Test
  void givenCapabilityAnnotation_whenCreateIsCalled_thenCapabilityAnnotationProcessorIsReturned() {
    assertInstanceOf(CapabilityAnnotationProcessor.class, classUnderTest.create(Ord.Capability.class));
  }

  @Test
  void givenConsumptionBundleAnnotation_whenCreateIsCalled_thenCorrectProcessorIsReturned() {
    assertInstanceOf(
        ConsumptionBundleAnnotationProcessor.class, classUnderTest.create(Ord.ConsumptionBundle.class));
  }

  @Test
  void givenDataProductAnnotation_whenCreateIsCalled_thenDataProductAnnotationProcessorIsReturned() {
    assertInstanceOf(DataProductAnnotationProcessor.class, classUnderTest.create(Ord.DataProduct.class));
  }

  @Test
  void givenEntityTypeAnnotation_whenCreateIsCalled_thenEntityTypeAnnotationProcessorIsReturned() {
    assertInstanceOf(EntityTypeAnnotationProcessor.class, classUnderTest.create(Ord.EntityType.class));
  }

  @Test
  void givenEventResourceAnnotation_whenCreateIsCalled_thenEventResourceAnnotationProcessorIsReturned() {
    assertInstanceOf(EventResourceAnnotationProcessor.class, classUnderTest.create(Ord.EventResource.class));
  }

  @Test
  void givenGroupAnnotation_whenCreateIsCalled_thenGroupAnnotationProcessorIsReturned() {
    assertInstanceOf(GroupAnnotationProcessor.class, classUnderTest.create(Ord.Group.class));
  }

  @Test
  void givenGroupTypeAnnotation_whenCreateIsCalled_thenGroupTypeAnnotationProcessorIsReturned() {
    assertInstanceOf(GroupTypeAnnotationProcessor.class, classUnderTest.create(Ord.GroupType.class));
  }

  @Test
  void givenIntegrationDependencyAnnotation_whenCreateIsCalled_thenCorrectProcessorIsReturned() {
    assertInstanceOf(
        IntegrationDependencyAnnotationProcessor.class, classUnderTest.create(Ord.IntegrationDependency.class));
  }

  @Test
  void givenPackageAnnotation_whenCreateIsCalled_thenPackageAnnotationProcessorIsReturned() {
    assertInstanceOf(PackageAnnotationProcessor.class, classUnderTest.create(Ord.Package.class));
  }

  @Test
  void givenProductAnnotation_whenCreateIsCalled_thenProductAnnotationProcessorIsReturned() {
    assertInstanceOf(ProductAnnotationProcessor.class, classUnderTest.create(Ord.Product.class));
  }

  @Test
  void givenSystemInstanceAnnotation_whenCreateIsCalled_thenCorrectProcessorIsReturned() {
    assertInstanceOf(SystemInstanceAnnotationProcessor.class, classUnderTest.create(Ord.SystemInstance.class));
  }

  @Test
  void givenSystemTypeAnnotation_whenCreateIsCalled_thenSystemTypeAnnotationProcessorIsReturned() {
    assertInstanceOf(SystemTypeAnnotationProcessor.class, classUnderTest.create(Ord.SystemType.class));
  }

  @Test
  void givenSystemVersionAnnotation_whenCreateIsCalled_thenCorrectProcessorIsReturned() {
    assertInstanceOf(SystemVersionAnnotationProcessor.class, classUnderTest.create(Ord.SystemVersion.class));
  }

  @Test
  void givenTombstoneAnnotation_whenCreateIsCalled_thenTombstoneAnnotationProcessorIsReturned() {
    assertInstanceOf(TombstoneAnnotationProcessor.class, classUnderTest.create(Ord.Tombstone.class));
  }

  @Test
  void givenVendorAnnotation_whenCreateIsCalled_thenVendorAnnotationProcessorIsReturned() {
    assertInstanceOf(VendorAnnotationProcessor.class, classUnderTest.create(Ord.Vendor.class));
  }

  @Test
  void givenUnknownAnnotation_whenCreateIsCalled_thenNullPointerExceptionIsThrown() {
    assertThrows(NullPointerException.class, () -> classUnderTest.create(Override.class));
  }

  @Test
  void whenCreateIsCalled_thenProcessorIsAutowired() {
    classUnderTest.create(Ord.Agent.class);

    verify(autowireCapableBeanFactory)
        .autowireBeanProperties(
            any(AgentAnnotationProcessor.class), eq(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE), eq(true));
  }
}
