package org.openresourcediscovery.core.services.impl;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.SetUtils.union;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import jakarta.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

@Slf4j
public class CachingOrdAnnotationsScannerImpl implements OrdAnnotationsScanner {

  private static final boolean USE_DEFAULT_FILTERS = false;

  private final OrdProperties properties;
  private final ApplicationContext applicationContext;
  private final Map<Class<? extends Annotation>, List<ScanResult<Annotation>>> cache;

  public CachingOrdAnnotationsScannerImpl(OrdProperties properties, ApplicationContext applicationContext) {
    this.properties = properties;
    this.cache = new ConcurrentHashMap<>();
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() {
    scan(Set.of(
        Ord.Agent.class,
        Ord.Group.class,
        Ord.Vendor.class,
        Ord.Package.class,
        Ord.Product.class,
        Ord.Document.class,
        Ord.GroupType.class,
        Ord.Tombstone.class,
        Ord.EntityType.class,
        Ord.SystemType.class,
        Ord.Capability.class,
        Ord.ApiResource.class,
        Ord.DataProduct.class,
        Ord.EventResource.class,
        Ord.SystemVersion.class,
        Ord.SystemInstance.class,
        Ord.ConsumptionBundle.class,
        Ord.IntegrationDependency.class));
  }

  @SuppressWarnings("unchecked")
  public <T extends Annotation> List<ScanResult<T>> scan(Class<T> annotation) {
    if (!cache.containsKey(annotation)) {
      scan(Set.of(annotation));
    }

    return emptyIfNull(cache.get(annotation)).stream()
        .map(p -> new ScanResult<>(p.annotated(), (T) p.annotation()))
        .collect(toCollection(ArrayList::new));
  }

  private Class<?> loadClass(BeanDefinition definition) {
    try {
      return Class.forName(definition.getBeanClassName(), true, applicationContext.getClassLoader());
    } catch (ClassNotFoundException e) {
      log.warn("Failed to load class for bean definition {}", definition.getBeanClassName(), e);
      return null;
    }
  }

  private void scan(Set<Class<? extends Annotation>> annotations) {
    Map<Class<? extends Annotation>, Class<? extends Annotation>> repeatable =
        lookupRepeatableAnnotations(annotations);
    Set<Class<? extends Annotation>> all = union(annotations, repeatable.keySet()).stream()
        .filter(not(cache::containsKey))
        .collect(toSet());

    all.forEach(annotation -> cache.put(annotation, new ArrayList<>()));

    properties.getPackages().forEach(pkg -> processPackage(pkg, all, repeatable));
  }

  private void processPackage(
      String pkg,
      Set<Class<? extends Annotation>> annotations,
      Map<Class<? extends Annotation>, Class<? extends Annotation>> repeatable) {
    findCandidateClasses(pkg, annotations)
        .forEach(
            candidate -> annotations.forEach(annotation -> ofNullable(findAnnotation(candidate, annotation))
                .ifPresent(instance -> cache(
                    repeatable.getOrDefault(annotation, annotation),
                    candidate,
                    (!repeatable.containsKey(annotation)
                        ? Stream.of(instance)
                        : extractRepeated(instance))))));
  }

  private void cache(Class<? extends Annotation> annotation, Class<?> candidate, Stream<Annotation> instances) {
    instances.forEach(instance -> cache.get(annotation).add(new ScanResult<>(candidate, instance)));
  }

  private List<? extends Class<?>> findCandidateClasses(String pkg, Set<Class<? extends Annotation>> annotations) {
    return createClassPathScanningCandidateComponentProvider(annotations).findCandidateComponents(pkg).stream()
        .map(this::loadClass)
        .filter(Objects::nonNull)
        .toList();
  }

  private Map<Class<? extends Annotation>, Class<? extends Annotation>> lookupRepeatableAnnotations(
      Set<Class<? extends Annotation>> annotations) {
    return annotations.stream()
        .filter(a -> nonNull(a.getAnnotation(Repeatable.class)))
        .collect(toMap(a -> a.getAnnotation(Repeatable.class).value(), Function.identity()));
  }

  private ClassPathScanningCandidateComponentProvider createClassPathScanningCandidateComponentProvider(
      Collection<Class<? extends Annotation>> annotations) {
    var result = new ClassPathScanningCandidateComponentProvider(USE_DEFAULT_FILTERS);

    result.setResourceLoader(applicationContext);
    result.setEnvironment(applicationContext.getEnvironment());
    annotations.stream().map(AnnotationTypeFilter::new).forEach(result::addIncludeFilter);

    return result;
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private static <T extends Annotation> Stream<T> extractRepeated(Annotation annotation) {
    return Stream.of((T[]) annotation.annotationType().getMethod("value").invoke(annotation));
  }
}
