package org.openresourcediscovery.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.multiset.HashMultiSet;

@UtilityClass
public class TLS {

  public static String decode(String header) {
    return isEmpty(header) ? EMPTY : new String(Base64.getDecoder().decode(header), UTF_8);
  }

  public static String[] tokenize(String dn) {
    return Stream.of(dn.split(dn.startsWith("/") ? "/" : ","))
        .map(String::trim)
        .filter(Predicate.not(String::isEmpty))
        .toArray(String[]::new);
  }

  public static boolean tokensMatch(String[] expected, String[] found) {
    return Objects.equals(new HashMultiSet<>(List.of(expected)), new HashMultiSet<>(List.of(found)));
  }
}
