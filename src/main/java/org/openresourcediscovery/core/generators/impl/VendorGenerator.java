package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Vendor;

@Setter(onMethod = @__({@Resource}))
public class VendorGenerator extends EntityAutoGenerator<Ord.Vendor, Vendor> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:vendor:%s:v1";

  public VendorGenerator() {
    super(Vendor::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Vendor> context, String field) {
    return switch (field) {
      case "title" -> context.annotated().getSimpleName();
      case "ordId" ->
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }
}
