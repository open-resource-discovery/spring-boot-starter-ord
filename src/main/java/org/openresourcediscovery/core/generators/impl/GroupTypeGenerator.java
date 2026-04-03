package org.openresourcediscovery.core.generators.impl;

import static java.util.Locale.ROOT;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.GroupType;

@Setter(onMethod = @__({@Resource}))
public class GroupTypeGenerator extends EntityAutoGenerator<Ord.GroupType, GroupType> {

  public GroupTypeGenerator() {
    super(GroupType::new);
  }

  @Override
  protected Object tryGenerateDefault(Context<Ord.GroupType> context, String field) {
    return switch (field) {
      case "title" -> context.annotated().getSimpleName();
      case "groupTypeId" -> asDefaultGroupTypeId(context.annotated());
      default -> null;
    };
  }

  private String asDefaultGroupTypeId(Class<?> clazz) {
    return "%s:%s"
        .formatted(ordProperties.getNamespace(), clazz.getSimpleName().toLowerCase(ROOT));
  }
}
