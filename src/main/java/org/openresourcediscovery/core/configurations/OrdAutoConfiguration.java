package org.openresourcediscovery.core.configurations;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

import org.openresourcediscovery.api.advices.OrdExceptionHandler;
import org.openresourcediscovery.api.controllers.OpenResourceDiscoveryController;
import org.openresourcediscovery.api.controllers.WellKnownController;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
@EnableConfigurationProperties(OrdProperties.class)
@ConditionalOnBooleanProperty(name = "ord.autoconfigure", matchIfMissing = true)
@Import({
  ServicesConfiguration.class,
  SecurityConfiguration.class,
  OrdExceptionHandler.class,
  WellKnownController.class,
  OpenResourceDiscoveryController.class,
})
public class OrdAutoConfiguration {}
