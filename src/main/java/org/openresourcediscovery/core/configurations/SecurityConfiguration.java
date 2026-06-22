package org.openresourcediscovery.core.configurations;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.openresourcediscovery.core.security.impl.AccessStrategiesResolverImpl;
import org.openresourcediscovery.core.security.impl.BasicAuthAuthenticatorImpl;
import org.openresourcediscovery.core.security.impl.OrdAuthorizationManagerImpl;
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.openresourcediscovery.core.services.StaticResourceRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

  @Bean
  @ConditionalOnMissingBean
  public AccessStrategiesResolver accessStrategiesResolver(
      OrdProperties ordProperties,
      DocumentSchemaRegistry documentSchemaRegistry,
      StaticResourceRegistry staticResourceRegistry) {
    return new AccessStrategiesResolverImpl(ordProperties, documentSchemaRegistry, staticResourceRegistry);
  }

  @Bean
  @ConditionalOnMissingBean(name = "ordBasicAuthenticator")
  public OrdAuthenticationManager ordBasicAuthenticator(AccessStrategiesResolver accessStrategiesResolver) {
    return new BasicAuthAuthenticatorImpl(accessStrategiesResolver, new AuthenticationTrustResolverImpl());
  }

  @Bean
  @ConditionalOnMissingBean
  public OrdAuthorizationManager ordAuthorizationManager(
      Collection<OrdAuthenticationManager> authenticators, AccessStrategiesResolver accessStrategiesResolver) {
    return new OrdAuthorizationManagerImpl(accessStrategiesResolver, authenticators);
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
      HttpSecurity http,
      OrdProperties ordProperties,
      @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .securityMatcher(ordProperties.getWellKnownPath())
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(auth -> auth.requestMatchers(GET, ordProperties.getWellKnownPath())
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
      OrdProperties ordProperties,
      OrdAuthorizationManager ordAuthorizationManager,
      @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .httpBasic(withDefaults())
        .securityMatcher(asWildcardPattern(ordProperties.getDocumentsPath()))
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(GET, asWildcardPattern(ordProperties.getDocumentsPath()))
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
      OrdProperties ordProperties,
      OrdAuthorizationManager ordAuthorizationManager,
      @Qualifier("ordUserDetailsService") UserDetailsService userDetailsService) {
    return http //
        .httpBasic(withDefaults())
        .securityMatcher(asWildcardPattern(ordProperties.getResourcesPath()))
        .csrf(AbstractHttpConfigurer::disable)
        .userDetailsService(userDetailsService)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(GET, asWildcardPattern(ordProperties.getResourcesPath()))
                .access(ordAuthorizationManager)
                .anyRequest()
                .denyAll())
        .build();
  }

  private static String asWildcardPattern(String path) {
    return "%s/*".formatted(path);
  }
}
