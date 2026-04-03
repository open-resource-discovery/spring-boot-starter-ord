package org.openresourcediscovery.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.openresourcediscovery.core.services.impl.EntityGeneratorFactoryImpl.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.impl.AgentGenerator;
import org.openresourcediscovery.core.generators.impl.ApiModelSelectorODataGenerator;
import org.openresourcediscovery.core.generators.impl.ApiResourceGenerator;
import org.openresourcediscovery.core.generators.impl.CapabilityGenerator;
import org.openresourcediscovery.core.generators.impl.ConsumptionBundleGenerator;
import org.openresourcediscovery.core.generators.impl.DataProductGenerator;
import org.openresourcediscovery.core.generators.impl.DocumentSchemaGenerator;
import org.openresourcediscovery.core.generators.impl.DocumentationLabelsGenerator;
import org.openresourcediscovery.core.generators.impl.EntityTypeGenerator;
import org.openresourcediscovery.core.generators.impl.EventResourceGenerator;
import org.openresourcediscovery.core.generators.impl.ExtensibleGenerator;
import org.openresourcediscovery.core.generators.impl.GroupGenerator;
import org.openresourcediscovery.core.generators.impl.GroupTypeGenerator;
import org.openresourcediscovery.core.generators.impl.IntegrationDependencyGenerator;
import org.openresourcediscovery.core.generators.impl.LabelsGenerator;
import org.openresourcediscovery.core.generators.impl.PackageGenerator;
import org.openresourcediscovery.core.generators.impl.ProductGenerator;
import org.openresourcediscovery.core.generators.impl.SystemInstanceGenerator;
import org.openresourcediscovery.core.generators.impl.SystemTypeGenerator;
import org.openresourcediscovery.core.generators.impl.SystemVersionGenerator;
import org.openresourcediscovery.core.generators.impl.VendorGenerator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

@ExtendWith(MockitoExtension.class)
class EntityGeneratorFactoryImplTest {

  @Mock
  private AutowireCapableBeanFactory autowireCapableBeanFactory;

  private EntityGeneratorFactoryImpl classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest =
        (EntityGeneratorFactoryImpl) builder(autowireCapableBeanFactory).build();
  }

  @Test
  void givenAgentAnnotation_whenCreateIsCalled_thenAgentGeneratorIsReturned() {
    assertInstanceOf(AgentGenerator.class, classUnderTest.create(Ord.Agent.class));
  }

  @Test
  void givenApiResourceAnnotation_whenCreateIsCalled_thenApiResourceGeneratorIsReturned() {
    assertInstanceOf(ApiResourceGenerator.class, classUnderTest.create(Ord.ApiResource.class));
  }

  @Test
  void givenApiModelSelectorODataAnnotation_whenCreateIsCalled_thenCorrectGeneratorIsReturned() {
    assertInstanceOf(ApiModelSelectorODataGenerator.class, classUnderTest.create(Ord.ApiModelSelectorOData.class));
  }

  @Test
  void givenCapabilityAnnotation_whenCreateIsCalled_thenCapabilityGeneratorIsReturned() {
    assertInstanceOf(CapabilityGenerator.class, classUnderTest.create(Ord.Capability.class));
  }

  @Test
  void givenConsumptionBundleAnnotation_whenCreateIsCalled_thenCorrectGeneratorIsReturned() {
    assertInstanceOf(ConsumptionBundleGenerator.class, classUnderTest.create(Ord.ConsumptionBundle.class));
  }

  @Test
  void givenDataProductAnnotation_whenCreateIsCalled_thenDataProductGeneratorIsReturned() {
    assertInstanceOf(DataProductGenerator.class, classUnderTest.create(Ord.DataProduct.class));
  }

  @Test
  void givenDocumentAnnotation_whenCreateIsCalled_thenDocumentSchemaGeneratorIsReturned() {
    assertInstanceOf(DocumentSchemaGenerator.class, classUnderTest.create(Ord.Document.class));
  }

  @Test
  void givenDocumentationLabelsAnnotation_whenCreateIsCalled_thenCorrectGeneratorIsReturned() {
    assertInstanceOf(DocumentationLabelsGenerator.class, classUnderTest.create(Ord.DocumentationLabels.class));
  }

  @Test
  void givenEntityTypeAnnotation_whenCreateIsCalled_thenEntityTypeGeneratorIsReturned() {
    assertInstanceOf(EntityTypeGenerator.class, classUnderTest.create(Ord.EntityType.class));
  }

  @Test
  void givenEventResourceAnnotation_whenCreateIsCalled_thenEventResourceGeneratorIsReturned() {
    assertInstanceOf(EventResourceGenerator.class, classUnderTest.create(Ord.EventResource.class));
  }

  @Test
  void givenExtensibleAnnotation_whenCreateIsCalled_thenExtensibleGeneratorIsReturned() {
    assertInstanceOf(ExtensibleGenerator.class, classUnderTest.create(Ord.Extensible.class));
  }

  @Test
  void givenGroupAnnotation_whenCreateIsCalled_thenGroupGeneratorIsReturned() {
    assertInstanceOf(GroupGenerator.class, classUnderTest.create(Ord.Group.class));
  }

  @Test
  void givenGroupTypeAnnotation_whenCreateIsCalled_thenGroupTypeGeneratorIsReturned() {
    assertInstanceOf(GroupTypeGenerator.class, classUnderTest.create(Ord.GroupType.class));
  }

  @Test
  void givenIntegrationDependencyAnnotation_whenCreateIsCalled_thenCorrectGeneratorIsReturned() {
    assertInstanceOf(IntegrationDependencyGenerator.class, classUnderTest.create(Ord.IntegrationDependency.class));
  }

  @Test
  void givenLabelsAnnotation_whenCreateIsCalled_thenLabelsGeneratorIsReturned() {
    assertInstanceOf(LabelsGenerator.class, classUnderTest.create(Ord.Labels.class));
  }

  @Test
  void givenPackageAnnotation_whenCreateIsCalled_thenPackageGeneratorIsReturned() {
    assertInstanceOf(PackageGenerator.class, classUnderTest.create(Ord.Package.class));
  }

  @Test
  void givenProductAnnotation_whenCreateIsCalled_thenProductGeneratorIsReturned() {
    assertInstanceOf(ProductGenerator.class, classUnderTest.create(Ord.Product.class));
  }

  @Test
  void givenSystemInstanceAnnotation_whenCreateIsCalled_thenSystemInstanceGeneratorIsReturned() {
    assertInstanceOf(SystemInstanceGenerator.class, classUnderTest.create(Ord.SystemInstance.class));
  }

  @Test
  void givenSystemTypeAnnotation_whenCreateIsCalled_thenSystemTypeGeneratorIsReturned() {
    assertInstanceOf(SystemTypeGenerator.class, classUnderTest.create(Ord.SystemType.class));
  }

  @Test
  void givenSystemVersionAnnotation_whenCreateIsCalled_thenSystemVersionGeneratorIsReturned() {
    assertInstanceOf(SystemVersionGenerator.class, classUnderTest.create(Ord.SystemVersion.class));
  }

  @Test
  void givenVendorAnnotation_whenCreateIsCalled_thenVendorGeneratorIsReturned() {
    assertInstanceOf(VendorGenerator.class, classUnderTest.create(Ord.Vendor.class));
  }

  @Test
  void givenUnknownAnnotation_whenCreateIsCalled_thenNullPointerExceptionIsThrown() {
    assertThrows(NullPointerException.class, () -> classUnderTest.create(Override.class));
  }

  @Test
  void whenCreateIsCalled_thenGeneratorIsAutowired() {
    classUnderTest.create(Ord.Agent.class);

    verify(autowireCapableBeanFactory)
        .autowireBeanProperties(
            any(AgentGenerator.class), eq(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE), eq(true));
  }
}
