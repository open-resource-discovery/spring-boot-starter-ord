package org.openresourcediscovery.core.generators.impl;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import jakarta.annotation.Resource;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityGenerator;

@NoArgsConstructor
@Setter(onMethod = @__({@Resource}))
public class EntityTypeOrdIdTargetGenerator extends EntityGenerator<Ord.EntityTypeOrdIdTarget, Object> {

  @Override
  public Object generate(Context<Ord.EntityTypeOrdIdTarget> context) {
    return customize(
        context,
        Map.ofEntries(
            /// Mandatory
            Map.entry(
                "ordId",
                requireNonNull(
                    firstNonBlank(context.annotation().ordId())))));
  }
}
