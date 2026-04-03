package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import org.openresourcediscovery.core.generators.EntityGenerator;

public interface EntityGeneratorFactory {

  <A extends Annotation, T> EntityGenerator<A, T> create(Class<A> annotation);
}
