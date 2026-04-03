package org.openresourcediscovery.core.generators.impl;

import static org.openresourcediscovery.utils.Commons.isEmpty;

import jakarta.annotation.Resource;
import lombok.Setter;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.generators.EntityAutoGenerator;
import org.openresourcediscovery.model.Extensible;

@Setter(onMethod = @__({@Resource}))
public class ExtensibleGenerator extends EntityAutoGenerator<Ord.Extensible, Extensible> {

  public ExtensibleGenerator() {
    super(Extensible::new);
  }

  @Override
  public Extensible generate(Context<Ord.Extensible> context) {
    return isEmpty(context.annotation()) ? null : super.generate(context);
  }
}
