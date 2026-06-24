package org.openresourcediscovery.core.processors;

import java.lang.annotation.Annotation;
import java.util.Map;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.model.DocumentSchema;

public interface AnnotationProcessor<A extends Annotation, E> {

  interface Customizer<A extends Annotation> {

    void customize(A annotation, DocumentSchema documentSchema);
  }

  void process(Map<String, DetectionResult> documents);
}
