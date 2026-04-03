package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.IntegrationDependency;

@Setter(onMethod = @__({@Resource}))
public class IntegrationDependencyGenerator
    extends EntityAutoGenerator<Ord.IntegrationDependency, IntegrationDependency> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:integrationDependency:%s:v1";

  public IntegrationDependencyGenerator() {
    super(IntegrationDependency::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.IntegrationDependency> context, String field) {
    return switch (field) {
      case "version" -> "1.0.0";
      case "mandatory" -> false;
      case "visibility" -> "public";
      case "releaseStatus" -> "active";
      case "title" -> context.annotated().getSimpleName();
      case "partOfPackage" -> resolveDefaultPackageOrdId(context);
      case "ordId" ->
        DEFAULT_ORD_ID_TEMPLATE.formatted(
            ordProperties.getNamespace(), context.annotated().getSimpleName());
      default -> null;
    };
  }

  private String resolveDefaultPackageOrdId(Context<Ord.IntegrationDependency> context) {
    return Optional.ofNullable(context.documentSchema().getPackages())
        .filter(packages -> packages.size() == 1)
        .map(packages -> packages.get(0).getOrdId())
        .orElse("%s:package:default:v1".formatted(ordProperties.getNamespace()));
  }
}
