package org.openresourcediscovery.core.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.core.services.OrdAuthorizationManager;
import org.openresourcediscovery.core.services.TLSAuthenticator;
import org.openresourcediscovery.core.services.impl.AnnotationProcessorFactoryImpl;
import org.openresourcediscovery.core.services.impl.CachingOrdAnnotationsScannerImpl;
import org.openresourcediscovery.core.services.impl.DocumentSchemaRegistryImpl;
import org.openresourcediscovery.core.services.impl.EntityGeneratorFactoryImpl;
import org.openresourcediscovery.core.services.impl.JavaAnnotationsDocumentSchemaDetector;
import org.openresourcediscovery.core.services.impl.OrdAuthorizationManagerImpl;
import org.openresourcediscovery.core.services.impl.StaticFileDocumentSchemaDetector;
import org.openresourcediscovery.utils.conditions.OnOrdDocumentsProvided;
import org.openresourcediscovery.utils.conditions.OnOrdPackagesProvided;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AuthenticationTrustResolver;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServicesConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public EntityGeneratorFactory entityGeneratorFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
    return new EntityGeneratorFactoryImpl(autowireCapableBeanFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public AnnotationProcessorFactory annotationProcessorFactory(
      AutowireCapableBeanFactory autowireCapableBeanFactory) {
    return new AnnotationProcessorFactoryImpl(autowireCapableBeanFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  @Conditional(OnOrdPackagesProvided.class)
  public OrdAnnotationsScanner annotationsScanner(
      OrdProperties ordProperties, ApplicationContext applicationContext) {
    return new CachingOrdAnnotationsScannerImpl(ordProperties, applicationContext);
  }

  @Bean
  @Conditional(OnOrdDocumentsProvided.class)
  public DocumentSchemaDetector staticFileDocumentSchemaDetector(
      ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    return new StaticFileDocumentSchemaDetector(objectMapper, resourceLoader);
  }

  @Bean
  @ConditionalOnMissingBean
  public TLSAuthenticator tlsAuthenticator() {
    return request -> false;
  }

  @Bean
  @ConditionalOnMissingBean
  public OrdAuthorizationManager ordAuthorizationManager(
      TLSAuthenticator tlsAuthenticator,
      DocumentSchemaRegistry documentSchemaRegistry,
      @Qualifier("ordAuthenticationTrustResolver") AuthenticationTrustResolver authenticationTrustResolver) {
    return new OrdAuthorizationManagerImpl(tlsAuthenticator, documentSchemaRegistry, authenticationTrustResolver);
  }

  @Bean
  @Conditional(OnOrdPackagesProvided.class)
  public DocumentSchemaDetector javaAnnotationsDocumentSchemaDetector(
      OrdAnnotationsScanner ordAnnotationsScanner,
      EntityGeneratorFactory entityGeneratorFactory,
      AnnotationProcessorFactory annotationProcessorFactory) {
    return new JavaAnnotationsDocumentSchemaDetector(
        ordAnnotationsScanner, entityGeneratorFactory, annotationProcessorFactory);
  }

  @Bean
  @SneakyThrows
  @ConditionalOnMissingBean
  public DocumentSchemaRegistry documentSchemaRegistry(
      ObjectMapper objectMapper,
      OrdProperties ordProperties,
      Collection<DocumentSchemaDetector> documentSchemaDetectors) {
    return documentSchemaDetectors.stream()
        .flatMap(detector -> detector.detect(ordProperties).entrySet().stream())
        .reduce(
            (DocumentSchemaRegistry) new DocumentSchemaRegistryImpl(objectMapper),
            (r, e) -> r.register(
                e.getKey(),
                e.getValue().getRight(),
                e.getValue().getLeft()),
            (l, r) -> l);
  }
}
