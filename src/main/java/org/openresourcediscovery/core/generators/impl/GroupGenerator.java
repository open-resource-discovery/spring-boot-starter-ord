package org.openresourcediscovery.core.generators.impl;

import static java.util.Locale.ROOT;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Group;

@Setter(onMethod = @__({@Resource}))
public class GroupGenerator extends EntityAutoGenerator<Ord.Group, Group> {

  public GroupGenerator() {
    super(Group::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.Group> context, String field) {
    return switch (field) {
      case "title" -> context.annotated().getSimpleName();
      case "groupTypeId" -> asDefaultGroupTypeId(context.annotated());
      case "groupId" -> asDefaultGroupId(context.annotated(), context.annotation());
      default -> null;
    };
  }

  private String asDefaultGroupTypeId(Class<?> clazz) {
    return "%s:%s"
        .formatted(ordProperties.getNamespace(), clazz.getSimpleName().toLowerCase(ROOT));
  }

  private String asDefaultGroupId(Class<?> clazz, Ord.Group annotation) {
    return "%s:%s:%s"
        .formatted(
            firstNonBlank(annotation.groupTypeId(), asDefaultGroupTypeId(clazz)),
            ordProperties.getNamespace(),
            clazz.getSimpleName());
  }
}
