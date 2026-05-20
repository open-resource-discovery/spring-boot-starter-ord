package org.openresourcediscovery.core.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

public interface AccessStrategiesResolver {

  Set<String> resolve(HttpServletRequest request);
}
