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
public class ApiModelSelectorODataGenerator extends EntityGenerator<Ord.ApiModelSelectorOData, Object> {

  @Override
  public Object generate(Context<Ord.ApiModelSelectorOData> context) {
    return Map.ofEntries(
        /// Mandatory
        Map.entry(
            "type",
            requireNonNull(firstNonBlank(context.annotation().type()))),
        Map.entry(
            "entitySetName",
            requireNonNull(firstNonBlank(context.annotation().entitySetName()))));
  }
}
