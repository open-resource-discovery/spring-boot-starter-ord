package org.openresourcediscovery.core.generators;

import jakarta.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.EntityGeneratorFactory;
import org.openresourcediscovery.model.DocumentSchema;
import org.springframework.beans.factory.ObjectProvider;

@NoArgsConstructor
@Setter(onMethod = @__({@Resource}))
public abstract class EntityGenerator<A extends Annotation, E> {

  public interface Customizer<A extends Annotation, E> {

    E customize(Context<A> context, E entity);
  }

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
  protected ObjectProvider<Customizer<A, E>> customizers;
  protected EntityGeneratorFactory entityGeneratorFactory;

  @Resource
  public void setCustomizers(ObjectProvider<Customizer<A, E>> customizers) {
    this.customizers = customizers;
  }

  public abstract E generate(Context<A> context);

  protected E customize(Context<A> context, E entity) {
    return Optional.ofNullable(customizers)
        .map(ObjectProvider::orderedStream)
        .orElse(Stream.empty())
        .reduce(entity, (e, customizer) -> customizer.customize(context, e), (l, r) -> l);
  }
}
