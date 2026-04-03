package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.ConsumptionBundle;

@Setter(onMethod = @__({@Resource}))
public class ConsumptionBundleGenerator extends EntityAutoGenerator<Ord.ConsumptionBundle, ConsumptionBundle> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:consumptionBundle:%s:v1";

  public ConsumptionBundleGenerator() {
    super(ConsumptionBundle::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.ConsumptionBundle> context, String field) {
    return switch (field) {
      case "title" -> context.annotated().getSimpleName();
      case "ordId" ->
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }
}
