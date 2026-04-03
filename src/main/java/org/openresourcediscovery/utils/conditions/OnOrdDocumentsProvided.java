package org.openresourcediscovery.utils.conditions;

import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.match;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

import java.util.function.Supplier;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnOrdDocumentsProvided extends SpringBootCondition {

  private final String propertyName;
  private final Supplier<ConditionMessage.Builder> messageBuilder;

  OnOrdDocumentsProvided() {
    this.propertyName = "ord.documents";
    this.messageBuilder = () -> ConditionMessage.forCondition("ORD document schema locations");
  }

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    ConditionMessage.Builder messageBuilder = this.messageBuilder.get();
    BindResult<?> property = Binder.get(context.getEnvironment())
        .bind(this.propertyName, Bindable.listOf(OrdProperties.Document.class));

    return property.isBound()
        ? match(messageBuilder.found("property").items(this.propertyName))
        : noMatch(messageBuilder.didNotFind("property").items(this.propertyName));
  }
}
