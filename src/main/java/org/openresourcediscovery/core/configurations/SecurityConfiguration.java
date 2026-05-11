package org.openresourcediscovery.core.configurations;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.openresourcediscovery.core.security.impl.OrdAuthenticationManagerImpl;
import org.openresourcediscovery.core.security.impl.OrdAuthorizationManagerImpl;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

  private static final String PATTERN_ORD_DOCUMENTS = "/ord/v1/documents/*";
  private static final String PATTERN_ORD_RESOURCES = "/ord/v1/resources/*";
  private static final String PATTERN_WELL_KNOWN = "/.well-known/open-resource-discovery";

  @Bean(defaultCandidate = false)
  @Qualifier("ordAuthenticationTrustResolver")
  @ConditionalOnMissingBean(name = "ordAuthenticationTrustResolver")
  public AuthenticationTrustResolver ordAuthenticationTrustResolver() {
    return new AuthenticationTrustResolverImpl();
  }

  @Bean
  @ConditionalOnMissingBean
  public OrdAuthenticationManager ordAuthenticationManager(
      TLSAuthenticator tlsAuthenticator,
      @Qualifier("ordAuthenticationTrustResolver") AuthenticationTrustResolver authenticationTrustResolver) {
    return new OrdAuthenticationManagerImpl(tlsAuthenticator, authenticationTrustResolver);
  }

  @Bean
  @ConditionalOnMissingBean
  public OrdAuthorizationManager ordAuthorizationManager(
      TLSAuthenticator tlsAuthenticator,
      DocumentSchemaRegistry documentSchemaRegistry,
      StaticResourceRegistry staticResourceRegistry,
      @Qualifier("ordAuthenticationTrustResolver") AuthenticationTrustResolver authenticationTrustResolver) {
    return new OrdAuthorizationManagerImpl(
        tlsAuthenticator, documentSchemaRegistry, staticResourceRegistry, authenticationTrustResolver);
  }

  @Bean(defaultCandidate = false)
  @Qualifier("ordUserDetailsService")
  @ConditionalOnMissingBean(name = "ordUserDetailsService")
  public UserDetailsService ordUserDetailsService(OrdProperties properties) {
    return new InMemoryUserDetailsManager(properties.getCredentials().entrySet().stream()
        .map(e -> User.withUsername(e.getKey()).password(e.getValue()).build())
        .toArray(UserDetails[]::new));
  }

  @Bean
  @SneakyThrows
  @Order(Ordered.HIGHEST_PRECEDENCE + 10)
  @ConditionalOnMissingBean(name = "ordWellKnownSecurityFilterChain")
  public SecurityFilterChain ordWellKnownSecurityFilterChain(
      HttpSecurity http, @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .securityMatcher(PATTERN_WELL_KNOWN)
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(auth -> auth.requestMatchers(GET, PATTERN_WELL_KNOWN)
            .anonymous()
            .anyRequest()
            .denyAll())
        .build();
  }

  @Bean
  @SneakyThrows
  @Order(Ordered.HIGHEST_PRECEDENCE + 20)
  @ConditionalOnMissingBean(name = "ordDocumentsSecurityFilterChain")
  public SecurityFilterChain ordDocumentsSecurityFilterChain(
      HttpSecurity http,
      OrdAuthorizationManager ordAuthorizationManager,
      @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .httpBasic(withDefaults())
        .securityMatcher(PATTERN_ORD_DOCUMENTS)
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(auth -> auth.requestMatchers(GET, PATTERN_ORD_DOCUMENTS)
            .access(ordAuthorizationManager)
            .anyRequest()
            .denyAll())
        .build();
  }

  @Bean
  @SneakyThrows
  @Order(Ordered.HIGHEST_PRECEDENCE + 30)
  @ConditionalOnMissingBean(name = "ordResourceSecurityFilterChain")
  public SecurityFilterChain ordResourcesSecurityFilterChain(
      HttpSecurity http,
      OrdAuthorizationManager ordAuthorizationManager,
      @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .httpBasic(withDefaults())
        .securityMatcher(PATTERN_ORD_RESOURCES)
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(auth -> auth.requestMatchers(GET, PATTERN_ORD_RESOURCES)
            .access(ordAuthorizationManager)
            .anyRequest()
            .denyAll())
        .build();
  }
}
