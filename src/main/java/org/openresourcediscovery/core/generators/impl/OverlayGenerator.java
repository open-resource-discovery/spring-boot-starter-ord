package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Overlay;

@Setter(onMethod = @__({@Resource}))
public class OverlayGenerator extends EntityAutoGenerator<Ord.Overlay, Overlay> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:overlay:%s:v1";

  public OverlayGenerator() {
    super(Overlay::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Overlay> context, String field) {
    return switch (field) {
      case "version" -> "1.0.0"; //
      case "visibility" -> "public"; //
      case "releaseStatus" -> "active"; //
      case "ordId" -> //
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }
}
