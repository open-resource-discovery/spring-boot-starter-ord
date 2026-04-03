package org.openresourcediscovery.utils;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.isNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

@UtilityClass
public class Commons {

  public static final Set<String> JAVA_RESERVED_KEYWORDS = Set.of(
      "true",
      "false",
      "null",
      "abstract",
      "assert",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "continue",
      "const",
      "default",
      "do",
      "double",
      "else",
      "enum",
      "exports",
      "extends",
      "final",
      "finally",
      "float",
      "for",
      "goto",
      "if",
      "implements",
      "import",
      "instanceof",
      "int",
      "interface",
      "long",
      "module",
      "native",
      "new",
      "package",
      "private",
      "protected",
      "public",
      "requires",
      "return",
      "short",
      "static",
      "strictfp",
      "super",
      "switch",
      "synchronized",
      "this",
      "throw",
      "throws",
      "transient",
      "try",
      "var",
      "void",
      "volatile",
      "while");

  public static <A extends Annotation> boolean isEmpty(A annotation) {
    return isNull(annotation)
        || Stream.of(annotation.annotationType().getDeclaredMethods())
            .allMatch(method -> Objects.deepEquals(method.getDefaultValue(), resolve(annotation, method)));
  }

  public static Date asDate(String annotation) {
    return ObjectUtils.isEmpty(annotation)
        ? null
        : Date.from(ISO_DATE_TIME
            .parse(annotation, java.time.LocalDate::from)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant());
  }

  @SafeVarargs
  public static <T> List<T> asList(T... elements) {
    return ObjectUtils.isEmpty(elements) ? null : List.of(elements);
  }

  @SneakyThrows
  private static Object resolve(Object value, Method method) {
    return method.invoke(value);
  }
}
