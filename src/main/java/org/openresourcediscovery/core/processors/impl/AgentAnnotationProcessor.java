package org.openresourcediscovery.core.processors.impl;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.core.generators.EntityGenerator.Context;
import org.openresourcediscovery.core.processors.AnnotationProcessor;
import org.openresourcediscovery.core.processors.AnnotationProcessor.Customizer;
import org.openresourcediscovery.core.services.DocumentSchemaDetector.DetectionResult;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.openresourcediscovery.model.Agent;
import org.openresourcediscovery.model.DocumentSchema;

@Setter(onMethod = @__({@Resource}))
public class AgentAnnotationProcessor implements AnnotationProcessor<Ord.Agent, Agent> {

  private OrdAnnotationsScanner ordAnnotationsScanner;
  private EntityGeneratorFactory entityGeneratorFactory;
  private Collection<Customizer<Ord.Agent>> customizers;

  @Override
  public void process(Map<String, DetectionResult> documents) {
    EntityGenerator<Ord.Agent, Agent> entityGenerator = entityGeneratorFactory.create(Ord.Agent.class);

    ordAnnotationsScanner.scan(Ord.Agent.class).forEach(a -> {
      DocumentSchema document =
          documents.get(a.annotation().partOfDocument().name()).document();

      document.setAgents(ListUtils.union(
          emptyIfNull(document.getAgents()),
          List.of(entityGenerator.generate(Context.of(a.annotation(), a.annotated(), document)))));

      emptyIfNull(customizers).forEach(customizer -> customizer.customize(a.annotation(), document));
    });
  }
}
