package org.openresourcediscovery.core.generators.impl;

import static org.openresourcediscovery.utils.Commons.isEmpty;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.SystemInstance;

@Setter(onMethod = @__({@Resource}))
public class SystemInstanceGenerator extends EntityAutoGenerator<Ord.SystemInstance, SystemInstance> {

  public SystemInstanceGenerator() {
    super(SystemInstance::new);
  }

  @Override
  public SystemInstance generate(Context<Ord.SystemInstance> context) {
    return isEmpty(context.annotation()) ? null : super.generate(context);
  }
}
