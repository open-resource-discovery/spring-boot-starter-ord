package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import java.util.List;

public interface OrdAnnotationsScanner {

  record ScanResult<T extends Annotation>(Class<?> annotated, T annotation) {}

  <T extends Annotation> List<ScanResult<T>> scan(Class<T> annotation);
}
