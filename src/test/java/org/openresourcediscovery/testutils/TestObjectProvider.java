package org.openresourcediscovery.testutils;

import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.lang.NonNull;

public class TestObjectProvider<T> implements org.springframework.beans.factory.ObjectProvider<T> {

  private final T[] objects;

  @SafeVarargs
  public TestObjectProvider(T... objects) {
    this.objects = objects;
  }

  @NonNull
  @Override
  public Stream<T> stream() {
    return Arrays.stream(objects);
  }
}
