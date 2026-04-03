package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.IntegrationAspect;

@Setter(onMethod = @__({@Resource}))
public class IntegrationAspectGenerator extends EntityAutoGenerator<Ord.IntegrationAspect, IntegrationAspect> {

  public IntegrationAspectGenerator() {
    super(IntegrationAspect::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.IntegrationAspect> context, String field) {
    return switch (field) {
      case "mandatory" -> false;
      case "title" -> context.annotated().getSimpleName();
      default -> null;
    };
  }
}
