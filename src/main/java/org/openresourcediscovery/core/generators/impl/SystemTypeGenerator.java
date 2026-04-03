package org.openresourcediscovery.core.generators.impl;

import static org.openresourcediscovery.utils.Commons.isEmpty;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.SystemType;

@Setter(onMethod = @__({@Resource}))
public class SystemTypeGenerator extends EntityAutoGenerator<Ord.SystemType, SystemType> {

  public SystemTypeGenerator() {
    super(SystemType::new);
  }

  @Override
  public SystemType generate(Context<Ord.SystemType> context) {
    return isEmpty(context.annotation()) ? null : super.generate(context);
  }
}
