package org.openresourcediscovery.core.generators.impl;

import static org.openresourcediscovery.utils.Commons.isEmpty;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.SystemVersion;

@Setter(onMethod = @__({@Resource}))
public class SystemVersionGenerator extends EntityAutoGenerator<Ord.SystemVersion, SystemVersion> {

  public SystemVersionGenerator() {
    super(SystemVersion::new);
  }

  @Override
  public SystemVersion generate(Context<Ord.SystemVersion> context) {
    return isEmpty(context.annotation()) ? null : super.generate(context);
  }
}
