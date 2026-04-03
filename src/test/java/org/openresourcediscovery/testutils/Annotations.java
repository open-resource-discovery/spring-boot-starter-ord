package org.openresourcediscovery.testutils;

import static org.mockito.Mockito.lenient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.mockito.Mockito;

public class Annotations {

  @SneakyThrows
  public static <T extends Annotation> T mock(Class<T> annotation) {
    return mock(annotation, Map.of());
  }

  @SneakyThrows
  public static <T extends Annotation> T mock(Class<T> annotation, Map<String, Object> fields) {
    T result = Mockito.mock(annotation);

    lenient().doReturn(annotation).when(result).annotationType();
    Stream.of(annotation.getDeclaredMethods()).forEach(method -> lenient()
        .when(invoke(result, method))
        .thenReturn(fields.getOrDefault(method.getName(), method.getDefaultValue())));

    return result;
  }

  @SneakyThrows
  private static <T extends Annotation> Object invoke(T annotation, Method method) {
    return method.invoke(annotation);
  }
}
