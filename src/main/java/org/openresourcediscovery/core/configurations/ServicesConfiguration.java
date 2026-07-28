package org.openresourcediscovery.core.configurations;

import static org.springframework.http.MediaType.parseMediaType;

import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory.AnnotationProcessorFactoryCustomizer;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.EntityGeneratorFactory.EntityGeneratorFactoryCustomizer;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry.StaticResource;
import org.openresourcediscovery.core.services.impl.AnnotationProcessorFactoryImpl;
import org.openresourcediscovery.core.services.impl.CachingOrdAnnotationsScannerImpl;
import org.openresourcediscovery.core.services.impl.DocumentSchemaRegistryImpl;
import org.openresourcediscovery.core.services.impl.EntityGeneratorFactoryImpl;
import org.openresourcediscovery.core.services.impl.JavaAnnotationsDocumentSchemaDetector;
import org.openresourcediscovery.core.services.impl.StaticFileDocumentSchemaDetector;
import org.openresourcediscovery.core.services.impl.StaticResourceRegistryImpl;
import org.openresourcediscovery.utils.conditions.OnOrdDocumentsProvided;
import org.openresourcediscovery.utils.conditions.OnOrdPackagesProvided;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServicesConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public EntityGeneratorFactory entityGeneratorFactory(
      AutowireCapableBeanFactory autowireCapableBeanFactory,
      Optional<EntityGeneratorFactoryCustomizer> customizer) {
    return customizer
        .orElse(builder -> builder)
        .customize(EntityGeneratorFactoryImpl.builder(autowireCapableBeanFactory))
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public AnnotationProcessorFactory annotationProcessorFactory(
      AutowireCapableBeanFactory autowireCapableBeanFactory,
      Optional<AnnotationProcessorFactoryCustomizer> customizer) {
    return customizer
        .orElse(builder -> builder)
        .customize(AnnotationProcessorFactoryImpl.builder(autowireCapableBeanFactory))
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  @Conditional(OnOrdPackagesProvided.class)
  public OrdAnnotationsScanner ordAnnotationsScanner(
      OrdProperties ordProperties, ApplicationContext applicationContext) {
    return new CachingOrdAnnotationsScannerImpl(ordProperties, applicationContext);
  }

  @Bean
  @Conditional(OnOrdDocumentsProvided.class)
  @ConditionalOnMissingBean(name = "ordStaticFileDocumentSchemaDetector")
  public DocumentSchemaDetector ordStaticFileDocumentSchemaDetector(
      JsonMapper jsonMapper, ResourceLoader resourceLoader) {
    return new StaticFileDocumentSchemaDetector(jsonMapper, resourceLoader);
  }

  @Bean
  @Conditional(OnOrdPackagesProvided.class)
  @ConditionalOnMissingBean(name = "ordJavaAnnotationsDocumentSchemaDetector")
  public DocumentSchemaDetector ordJavaAnnotationsDocumentSchemaDetector(
      OrdAnnotationsScanner ordAnnotationsScanner,
      EntityGeneratorFactory entityGeneratorFactory,
      AnnotationProcessorFactory annotationProcessorFactory) {
    return new JavaAnnotationsDocumentSchemaDetector(
        ordAnnotationsScanner, entityGeneratorFactory, annotationProcessorFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public StaticResourceRegistry staticResourceRegistry(OrdProperties ordProperties, ResourceLoader resourceLoader) {
    return ordProperties.getApiResources().stream()
        .reduce(
            new StaticResourceRegistryImpl(),
            (registry, resource) -> registry.register(
                resource.getName(),
                resource.getAccessStrategies(),
                new StaticResource(
                    resourceLoader.getResource(resource.getPath()),
                    parseMediaType(resource.getMediaType()))),
            (l, r) -> l);
  }

  @Bean
  @ConditionalOnMissingBean
  public DocumentSchemaRegistry documentSchemaRegistry(
      JsonMapper jsonMapper,
      OrdProperties ordProperties,
      Collection<DocumentSchemaDetector> documentSchemaDetectors) {
    return documentSchemaDetectors.stream()
        .flatMap(detector -> detector.detect(ordProperties).entrySet().stream())
        .reduce(
            new DocumentSchemaRegistryImpl(jsonMapper),
            (r, e) -> r.register(
                e.getKey(),
                e.getValue().strategies(),
                e.getValue().document()),
            (l, r) -> l);
  }
}
