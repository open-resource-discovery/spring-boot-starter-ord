package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.DataProduct;
import org.openresourcediscovery.model.DataProductOutputPort;

@Setter(onMethod = @__({@Resource}))
public class DataProductGenerator extends EntityAutoGenerator<Ord.DataProduct, DataProduct> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:dataProduct:%s:v1";
  private static final String DEFAULT_DESCRIPTION_TEMPLATE = "Auto-generated description for %s";
  private static final String DEFAULT_SHORT_DESCRIPTION_TEMPLATE = "Auto-generated short description for %s";

  public DataProductGenerator() {
    super(DataProduct::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.DataProduct> context, String field) {
    return switch (field) {
      case "type" -> "primary";
      case "version" -> "1.0.0";
      case "category" -> "other";
      case "visibility" -> "public";
      case "releaseStatus" -> "active";
      case "title" -> context.annotated().getSimpleName();
      case "partOfPackage" -> resolveDefaultPackageOrdId(context);
      case "responsible" -> asDefaultResponsible(context.annotated());
      case "outputPorts" -> asDefaultDataProductOutputPorts(context.annotated());
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

  private String asDefaultResponsible(Class<?> clazz) {
    return "%s:%s:%s"
        .formatted(
            ordProperties.getNamespace().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", ""),
            ordProperties.getApplication().replaceAll("[^A-Za-z0-9._\\-/]", ""),
            clazz.getSimpleName().replaceAll("[^A-Za-z0-9._\\-/]", ""));
  }

  private String resolveDefaultPackageOrdId(Context<Ord.DataProduct> context) {
    return Optional.ofNullable(context.documentSchema().getPackages())
        .filter(packages -> packages.size() == 1)
        .map(packages -> packages.get(0).getOrdId())
        .orElse("%s:package:default:v1".formatted(ordProperties.getNamespace()));
  }

  private List<DataProductOutputPort> asDefaultDataProductOutputPorts(Class<?> clazz) {
    return List.of(new DataProductOutputPort()
        .withOrdId("%s:apiResource:%s:v1"
            .formatted(
                ordProperties
                    .getNamespace()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9.]", ""),
                clazz.getSimpleName().replaceAll("[^A-Za-z0-9._\\-]", ""))));
  }
}
