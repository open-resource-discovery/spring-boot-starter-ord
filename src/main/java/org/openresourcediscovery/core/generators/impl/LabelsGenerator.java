package org.openresourcediscovery.core.generators.impl;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.openresourcediscovery.utils.Commons.asList;

import jakarta.annotation.Resource;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;
import org.openresourcediscovery.model.Labels;

@NoArgsConstructor
@Setter(onMethod = @__({@Resource}))
public class LabelsGenerator extends EntityGenerator<Ord.Labels, Labels> {

  @Override
  public Labels generate(Context<Ord.Labels> context) {
    return isEmpty(context.annotation().value())
        ? null
        : Stream.of(context.annotation().value())
            .reduce(
                new Labels(),
                (l, a) -> l.withAdditionalProperty(a.key(), asList((a.values()))),
                (l, r) -> l);
  }
}
