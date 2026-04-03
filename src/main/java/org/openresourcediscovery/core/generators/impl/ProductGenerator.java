package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Product;

@Setter(onMethod = @__({@Resource}))
public class ProductGenerator extends EntityAutoGenerator<Ord.Product, Product> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:product:%s:";
  private static final String DEFAULT_SHORT_DESCRIPTION_TEMPLATE = "Auto-generated short description for %s";

  public ProductGenerator() {
    super(Product::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Product> context, String field) {
    return switch (field) {
      case "vendor" -> "customer:vendor:Customer:";
      case "title" -> context.annotated().getSimpleName();
      case "shortDescription" ->
        DEFAULT_SHORT_DESCRIPTION_TEMPLATE.formatted(context.annotated().getSimpleName());
      case "ordId" ->
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }
}
