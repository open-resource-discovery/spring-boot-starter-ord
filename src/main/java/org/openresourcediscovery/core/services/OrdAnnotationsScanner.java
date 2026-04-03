package org.openresourcediscovery.core.services;

import java.lang.annotation.Annotation;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface OrdAnnotationsScanner {

  <T extends Annotation> List<Pair<? extends Class<?>, T>> scan(Class<T> annotation);
}
