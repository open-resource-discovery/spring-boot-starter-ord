package org.openresourcediscovery.core.services;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public interface StaticResourceRegistry {

  record StaticResource(Resource resource, MediaType mediaType) {}

  Map<String, StaticResource> getAll();

  Set<String> lookupAccessStrategies(String name);

  Optional<StaticResource> lookupStaticResource(String name);
}
