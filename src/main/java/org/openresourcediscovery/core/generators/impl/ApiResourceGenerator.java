package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.ApiResource;

@Setter(onMethod = @__({@Resource}))
public class ApiResourceGenerator extends EntityAutoGenerator<Ord.ApiResource, ApiResource> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:apiResource:%s:v1";
  private static final String DEFAULT_DESCRIPTION_TEMPLATE = "Auto-generated description for %s";
  private static final String DEFAULT_SHORT_DESCRIPTION_TEMPLATE = "Auto-generated short description for %s";

  public ApiResourceGenerator() {
    super(ApiResource::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.ApiResource> context, String field) {
    return switch (field) {
      case "version" -> "1.0.0";
      case "apiProtocol" -> "rest";
      case "visibility" -> "public";
      case "releaseStatus" -> "active";
      case "title" -> context.annotated().getSimpleName();
      case "partOfPackage" -> resolveDefaultPackageOrdId(context);
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

  private String resolveDefaultPackageOrdId(Context<Ord.ApiResource> context) {
    return Optional.ofNullable(context.documentSchema().getPackages())
        .filter(packages -> packages.size() == 1)
        .map(packages -> packages.get(0).getOrdId())
        .orElse("%s:package:default:v1".formatted(ordProperties.getNamespace()));
  }
}
