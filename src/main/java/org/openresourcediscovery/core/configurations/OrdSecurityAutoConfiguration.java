package org.openresourcediscovery.core.configurations;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@Import(SecurityConfiguration.class)
@ConditionalOnWebApplication(type = SERVLET)
@AutoConfigureAfter(OrdAutoConfiguration.class)
@ConditionalOnBooleanProperty(name = "ord.autoconfigure", matchIfMissing = true)
@ConditionalOnClass({HttpSecurity.class, SecurityFilterChain.class, AuthenticationTrustResolver.class})
public class OrdSecurityAutoConfiguration {}
