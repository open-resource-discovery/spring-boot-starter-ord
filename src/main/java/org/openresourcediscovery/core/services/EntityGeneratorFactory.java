package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Supplier;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public interface EntityGeneratorFactory {

  @FunctionalInterface
  interface EntityGeneratorFactoryCustomizer {

    EntityGeneratorFactoryBuilder customize(EntityGeneratorFactoryBuilder builder);
  }

  interface EntityGeneratorFactoryBuilder {

    EntityGeneratorFactoryBuilder withAutowireCapableBeanFactory(
        AutowireCapableBeanFactory autowireCapableBeanFactory);

    EntityGeneratorFactoryBuilder withSuppliers(
        Map<Class<? extends Annotation>, Supplier<EntityGenerator<? extends Annotation, ?>>> suppliers);

    EntityGeneratorFactoryBuilder withSupplier(
        Class<? extends Annotation> annotation, Supplier<EntityGenerator<? extends Annotation, ?>> supplier);

    EntityGeneratorFactory build();
  }

  <A extends Annotation, T> EntityGenerator<A, T> create(Class<A> annotation);
}
