package org.openresourcediscovery.core.generators;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.openresourcediscovery.utils.Commons.JAVA_RESERVED_KEYWORDS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openresourcediscovery.model.Extensible.Supported;
import org.openresourcediscovery.utils.Commons;

@RequiredArgsConstructor
public class EntityAutoGenerator<A extends Annotation, E> extends EntityGenerator<A, E> {

  private final Supplier<E> supplier;

  @Override
  @SneakyThrows
  public E generate(Context<A> context) {
    E entity = this.supplier.get();
    Set<String> required = extractRequiredFields(context.annotation());

    processPlain(context, entity, required);
    processPlainArrays(context, entity, required);
    processAnnotations(context, entity, required);
    processAnnotationArrays(context, entity, required);

    return entity;
  }

  protected Object tryGenerateDefault(Context<A> context, String field) {
    return null;
  }

  private void processPlain(Context<A> context, E entity, Set<String> required) {
    Stream.of(context.annotation().annotationType().getDeclaredMethods())
        .filter(EntityAutoGenerator::looksLikePlainGetter)
        .forEach(getter -> {
          lookupSetterFor(entity.getClass(), getter.getName()).ifPresent(setter -> {
            Object value = invoke(getter, context.annotation());
            boolean isRequired = required.contains(getter.getName());
            Object extracted = convert(value, setter.getParameterTypes()[0]);
            Object target = !ObjectUtils.isEmpty(extracted)
                ? extracted
                : tryGenerateDefault(context, getter.getName());

            assertTrue(areCompatible(setter, target), "Incompatible annotation values");
            assertTrue(
                !(isRequired && ObjectUtils.isEmpty(target)),
                "No value present for required field %s".formatted(getter.getName()));

            invoke(setter, entity, target);
          });
        });
  }

  private void processPlainArrays(Context<A> context, E entity, Set<String> required) {
    Stream.of(context.annotation().annotationType().getDeclaredMethods())
        .filter(EntityAutoGenerator::looksLikePlainArrayGetter)
        .forEach(getter -> {
          lookupSetterFor(entity.getClass(), getter.getName()).ifPresent(setter -> {
            Class<?> typ = resolveGenericType(setter);
            boolean isRequired = required.contains(getter.getName());
            Object extracted = Commons.asList(
                (Stream.of(ArrayUtils.nullToEmpty((Object[]) invoke(getter, context.annotation()))))
                    .map(v -> convert(v, typ))
                    .toArray());
            Object target = !ObjectUtils.isEmpty(extracted)
                ? extracted
                : tryGenerateDefault(context, getter.getName());

            assertTrue(areCompatible(setter, target), "Incompatible annotation values");
            assertTrue(
                !(isRequired && ObjectUtils.isEmpty(target)),
                "No value present for required field %s".formatted(getter.getName()));

            invoke(setter, entity, target);
          });
        });
  }

  @SuppressWarnings("unchecked")
  private EntityGenerator<Annotation, ?> lookupEntityGenerator(Class<?> annotation) {
    return (EntityGenerator<Annotation, ?>) entityGeneratorFactory.create((Class<? extends Annotation>) annotation);
  }

  private void processAnnotations(Context<A> context, E entity, Set<String> required) {
    Stream.of(context.annotation().annotationType().getDeclaredMethods())
        .filter(EntityAutoGenerator::looksLikeAnnotationGetter)
        .filter(getter -> !Commons.isEmpty((Annotation) invoke(getter, context.annotation())))
        .forEach(getter -> {
          lookupSetterFor(entity.getClass(), getter.getName()).ifPresent(setter -> {
            Class<?> source = getter.getReturnType();
            boolean isRequired = required.contains(getter.getName());
            Annotation value = (Annotation) invoke(getter, context.annotation());
            Object extracted = lookupEntityGenerator(source)
                .generate(Context.of(value, context.annotated(), context.documentSchema()));
            Object target = !ObjectUtils.isEmpty(extracted)
                ? extracted
                : tryGenerateDefault(context, getter.getName());

            assertTrue(
                !(isRequired && ObjectUtils.isEmpty(target)),
                "No value present for required field %s".formatted(getter.getName()));

            invoke(setter, entity, target);
          });
        });
  }

  private void processAnnotationArrays(Context<A> context, E entity, Set<String> required) {
    Stream.of(context.annotation().annotationType().getDeclaredMethods())
        .filter(EntityAutoGenerator::looksLikeAnnotationArrayGetter)
        .forEach(getter -> {
          lookupSetterFor(entity.getClass(), getter.getName()).ifPresent(setter -> {
            boolean isRequired = required.contains(getter.getName());
            Annotation[] values = (Annotation[]) invoke(getter, context.annotation());
            EntityGenerator<Annotation, ?> generator =
                lookupEntityGenerator(getter.getReturnType().getComponentType());
            Object extracted = Commons.asList((Stream.of(ArrayUtils.nullTo(values, new Annotation[0]))
                .filter(Predicate.not(Commons::isEmpty))
                .map(a -> generator.generate(
                    Context.of(a, context.annotated(), context.documentSchema())))
                .toArray()));
            Object target = !ObjectUtils.isEmpty(extracted)
                ? extracted
                : tryGenerateDefault(context, getter.getName());

            assertTrue(
                !(isRequired && ObjectUtils.isEmpty(target)),
                "No value present for required field %s".formatted(getter.getName()));

            invoke(setter, entity, target);
          });
        });
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException("Assertion failed: %s".formatted(message));
    }
  }

  @SneakyThrows
  private static Field asField(Method method) {
    return method.getDeclaringClass().getDeclaredField(asFieldName(method));
  }

  private static String asFieldName(Method method) {
    return uncapitalize(method.getName().replaceFirst("^(set|get|with)", ""));
  }

  private static String normalizeFieldName(String name) {
    return !name.startsWith("_") || !JAVA_RESERVED_KEYWORDS.contains(name.substring(1)) ? name : name.substring(1);
  }

  private static boolean looksLikeGetter(Method method) {
    return method.getParameterCount() == 0 && !Objects.equals(void.class, method.getReturnType());
  }

  @SneakyThrows
  private static Class<?> resolveGenericType(Method method) {
    return (Class<?>) ((ParameterizedType) asField(method).getGenericType()).getActualTypeArguments()[0];
  }

  private static boolean looksLikePlainGetter(Method method) {
    return looksLikeGetter(method)
        && !(method.getReturnType().isArray() || method.getReturnType().isAnnotation());
  }

  private static Object convert(Object value, Class<?> target) {
    if (ObjectUtils.isEmpty(value)) {
      return null;
    }

    if (Objects.equals(String.class, value.getClass()) && Objects.equals(URI.class, target)) {
      return URI.create(value.toString().trim());
    }

    if (Objects.equals(String.class, value.getClass()) && Objects.equals(Date.class, target)) {
      return Commons.asDate(value.toString().trim());
    }

    if (Objects.equals(String.class, value.getClass()) && Objects.equals(String.class, target)) {
      return value.toString().trim();
    }

    if (Objects.equals(String.class, value.getClass()) && Objects.equals(Supported.class, target)) {
      return Supported.fromValue(value.toString().trim());
    }

    return target.cast(value);
  }

  private static boolean looksLikeAnnotationGetter(Method method) {
    return looksLikeGetter(method) && method.getReturnType().isAnnotation();
  }

  private static boolean looksLikePlainArrayGetter(Method method) {
    return looksLikeGetter(method)
        && method.getReturnType().isArray()
        && !method.getReturnType().getComponentType().isAnnotation();
  }

  private static boolean areCompatible(Method setter, Object value) {
    return isNull(value)
        || primitiveToWrapper(setter.getParameterTypes()[0]).isAssignableFrom(value.getClass());
  }

  private static boolean looksLikeAnnotationArrayGetter(Method method) {
    return looksLikeGetter(method)
        && method.getReturnType().isArray()
        && method.getReturnType().getComponentType().isAnnotation();
  }

  @SneakyThrows
  private static Set<String> extractRequiredFields(Annotation annotation) {
    return Set.of(getDeclaredMethod(annotation.annotationType(), "requiredFields")
        .map(method -> (String[]) invoke(method, annotation))
        .orElse(new String[0]));
  }

  @SneakyThrows
  private static Object invoke(Method method, Object target, Object... args) {
    return method.invoke(target, args);
  }

  @SneakyThrows
  private static Optional<Method> getDeclaredMethod(Class<?> clazz, String method) {
    try {
      return Optional.of(clazz.getDeclaredMethod(method));
    } catch (NoSuchMethodException exception) {
      return Optional.empty();
    }
  }

  private static <E> Optional<Method> lookupSetterFor(Class<E> clazz, String name) {
    String setter = "set%s".formatted(capitalize(normalizeFieldName(name)));

    return Stream.of(clazz.getDeclaredMethods())
        .filter(method -> method.getParameterCount() == 1)
        .filter(method -> Objects.equals(setter, method.getName()))
        .findFirst();
  }
}
