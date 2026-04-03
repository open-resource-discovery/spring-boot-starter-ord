package org.openresourcediscovery.utils.conditions;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.OnPropertyListCondition;

public class OnOrdPackagesProvided extends OnPropertyListCondition {

  OnOrdPackagesProvided() {
    super("ord.packages", () -> ConditionMessage.forCondition("ORD annotated classes package scan"));
  }
}
