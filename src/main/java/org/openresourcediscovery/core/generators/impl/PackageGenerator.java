package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Package;

@Setter(onMethod = @__({@Resource}))
public class PackageGenerator extends EntityAutoGenerator<Ord.Package, Package> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:package:%s:v1";
  private static final String DEFAULT_DESCRIPTION_TEMPLATE = "Auto-generated description for %s";
  private static final String DEFAULT_SHORT_DESCRIPTION_TEMPLATE = "Auto-generated short description for %s";

  public PackageGenerator() {
    super(Package::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Package> context, String field) {
    return switch (field) {
      case "version" -> "1.0.0";
      case "vendor" -> "customer:vendor:Customer:";
      case "title" -> context.annotated().getSimpleName();
      case "description" ->
        DEFAULT_DESCRIPTION_TEMPLATE.formatted(context.annotated().getSimpleName());
      case "shortDescription" ->
        DEFAULT_SHORT_DESCRIPTION_TEMPLATE.formatted(context.annotated().getSimpleName());
      case "ordId" ->
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }
}
