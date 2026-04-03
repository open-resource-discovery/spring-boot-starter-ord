package org.openresourcediscovery.core.generators.impl;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.model.DocumentationLabels;

@NoArgsConstructor
@Setter(onMethod = @__({@Resource}))
public class DocumentationLabelsGenerator extends EntityGenerator<Ord.DocumentationLabels, DocumentationLabels> {

  @Override
  public DocumentationLabels generate(Context<Ord.DocumentationLabels> context) {
    return isEmpty(context.annotation().value())
        ? null
        : Stream.of(context.annotation().value())
            .reduce(
                new DocumentationLabels(),
                (l, a) -> l.withAdditionalProperty(a.key(), List.of(a.values())),
                (l, r) -> l);
  }
}
