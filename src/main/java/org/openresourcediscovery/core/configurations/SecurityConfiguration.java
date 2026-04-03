package org.openresourcediscovery.core.configurations;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.Customizer.withDefaults;

import lombok.SneakyThrows;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.OrdAuthorizationManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfiguration {

  private static final String PATTERN_ORD_DOCUMENTS = "/ord/v1/documents/*";
  private static final String PATTERN_WELL_KNOWN = "/.well-known/open-resource-discovery";

  @Bean(defaultCandidate = false)
  @Qualifier("ordAuthenticationTrustResolver")
  public AuthenticationTrustResolver authenticationTrustResolver() {
    return new AuthenticationTrustResolverImpl();
  }

  @Bean(defaultCandidate = false)
  @Qualifier("ordUserDetailsService")
  public UserDetailsService userDetailsService(OrdProperties properties) {
    return new InMemoryUserDetailsManager(properties.getCredentials().entrySet().stream()
        .map(e -> User.withUsername(e.getKey()).password(e.getValue()).build())
        .toArray(UserDetails[]::new));
  }

  @Bean
  @Order(1)
  @SneakyThrows
  public SecurityFilterChain wellKnownSecurityFilterChain(
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
  @Order(2)
  @SneakyThrows
  public SecurityFilterChain documentsSecurityFilterChain(
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
}
