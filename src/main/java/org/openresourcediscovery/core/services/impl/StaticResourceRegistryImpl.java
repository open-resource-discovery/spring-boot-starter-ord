package org.openresourcediscovery.core.services.impl;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openresourcediscovery.core.services.StaticResourceRegistry;

public class StaticResourceRegistryImpl implements StaticResourceRegistry {

  private final Map<String, StaticResource> resources;
  private final Map<String, Set<String>> accessStrategies;

  public StaticResourceRegistryImpl() {
    this.resources = new ConcurrentHashMap<>();
    this.accessStrategies = new ConcurrentHashMap<>();
  }

  public StaticResourceRegistryImpl register(String name, Set<String> strategies, StaticResource resource) {
    resources.put(name, resource);
    accessStrategies.put(name, strategies);

    return this;
  }

  @Override
  public Map<String, StaticResource> getAll() {
    return Map.copyOf(resources);
  }

  @Override
  public Set<String> lookupAccessStrategies(String name) {
    return unmodifiableSet(accessStrategies.getOrDefault(name, emptySet()));
  }

  @Override
  public Optional<StaticResource> lookupStaticResource(String name) {
    return Optional.ofNullable(resources.get(name));
  }
}
