package org.openresourcediscovery.core.generators;

import jakarta.annotation.Resource;
import java.lang.annotation.Annotation;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;

@NoArgsConstructor
@Setter(onMethod = @__({@Resource}))
public abstract class EntityGenerator<A extends Annotation, E> {

  @Value
  @Accessors(fluent = true)
  public static class Context<A extends Annotation> {

    A annotation;
    Class<?> annotated;
    DocumentSchema documentSchema;

    public static <A extends Annotation> Context<A> of(
        A annotation, Class<?> clazz, DocumentSchema documentSchema) {
      return new Context<>(annotation, clazz, documentSchema);
    }
  }

  protected OrdProperties ordProperties;
  protected EntityGeneratorFactory entityGeneratorFactory;

  public abstract E generate(Context<A> context);
}
