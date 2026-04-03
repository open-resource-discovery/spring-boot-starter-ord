package org.openresourcediscovery.core.processors;

import java.lang.annotation.Annotation;
import java.util.Map;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;

public interface AnnotationProcessor<A extends Annotation, E> {

  void process(Map<String, DetectionResult> documents);
}
