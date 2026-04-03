package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Supplier;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public interface AnnotationProcessorFactory {

  @FunctionalInterface
  interface AnnotationProcessorFactoryCustomizer {

    AnnotationProcessorFactoryBuilder customize(AnnotationProcessorFactoryBuilder builder);
  }

  interface AnnotationProcessorFactoryBuilder {

    AnnotationProcessorFactoryBuilder withAutowireCapableBeanFactory(
        AutowireCapableBeanFactory autowireCapableBeanFactory);

    AnnotationProcessorFactoryBuilder withSuppliers(
        Map<Class<? extends Annotation>, Supplier<AnnotationProcessor<? extends Annotation, ?>>> suppliers);

    AnnotationProcessorFactoryBuilder withSupplier(
        Class<? extends Annotation> annotation,
        Supplier<AnnotationProcessor<? extends Annotation, ?>> supplier);

    AnnotationProcessorFactory build();
  }

  <A extends Annotation, T> AnnotationProcessor<A, T> create(Class<A> annotation);
}
