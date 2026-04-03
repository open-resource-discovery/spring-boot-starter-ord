package org.openresourcediscovery.core.generators.impl;

import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Agent;

@Setter(onMethod = @__({@Resource}))
public class AgentGenerator extends EntityAutoGenerator<Ord.Agent, Agent> {

  private static final String DEFAULT_ORD_ID_TEMPLATE = "%s:agent:%s:v1";

  public AgentGenerator() {
    super(Agent::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Agent> context, String field) {
    return switch (field) {
      case "version" -> "1.0.0";
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

  private String resolveDefaultPackageOrdId(Context<Ord.Agent> context) {
    return Optional.ofNullable(context.documentSchema().getPackages())
        .filter(packages -> packages.size() == 1)
        .map(packages -> packages.get(0).getOrdId())
        .orElse("%s:package:default:v1".formatted(ordProperties.getNamespace()));
  }
}
