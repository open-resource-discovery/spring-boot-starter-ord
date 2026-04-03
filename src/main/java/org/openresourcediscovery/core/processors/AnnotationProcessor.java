package org.openresourcediscovery.core.processors;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.openresourcediscovery.model.DocumentSchema;

public interface AnnotationProcessor<A extends Annotation, E> {

  void process(Map<String, Pair<DocumentSchema, Set<String>>> documents);
}
