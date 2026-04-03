package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import org.openresourcediscovery.core.processors.AnnotationProcessor;

public interface AnnotationProcessorFactory {

  <A extends Annotation, T> AnnotationProcessor<A, T> create(Class<A> annotation);
}
