# Spring Security Configuration — Reference

How `spring-boot-starter-ord` configures Spring Security, what each piece does, and how to customise or replace any part of it.

**Contents:** [Activation conditions](#activation-conditions) · [What gets auto-configured](#what-gets-auto-configured) · [Authentication flow diagram](#authentication-flow-diagram) · [Credential configuration](#credential-configuration) · [Customization](#customization) · [Interaction with host app security](#interaction-with-the-host-applications-security) · [Disabling security](#disabling-security-entirely)

---

## Activation conditions

The security auto-configuration (`OrdSecurityAutoConfiguration`) activates only when **all** of the following are true:

- The application is a servlet web application
- `spring-security-web` and `spring-security-config` are on the classpath (i.e. `spring-boot-starter-security` is a dependency)
- `ord.autoconfigure` is not set to `false`

If Spring Security is absent from the classpath the endpoints are registered but completely unprotected — the starter provides no security of its own. Add `spring-boot-starter-security` to get the filter chains described below.

---

## What gets auto-configured

`SecurityConfiguration` (imported by `OrdSecurityAutoConfiguration`) registers the following beans.

### Two `SecurityFilterChain` beans

Both chains are scoped to ORD paths only and are ordered before any application-defined chains so they do not interfere with the rest of the application.

| Bean name | Order | Path pattern | Behaviour |
|---|---|---|---|
| `ordWellKnownSecurityFilterChain` | `HIGHEST_PRECEDENCE + 10` | `/.well-known/open-resource-discovery` | `GET` is always anonymous. All other methods are denied. |
| `ordDocumentsSecurityFilterChain` | `HIGHEST_PRECEDENCE + 20` | `/ord/v1/documents/*` | `GET` is delegated to `OrdAuthorizationManager`. All other methods are denied. HTTP Basic is enabled for credential exchange. |
| `ordResourcesSecurityFilterChain` | `HIGHEST_PRECEDENCE + 30` | `/ord/v1/resources/*` | `GET` is delegated to `OrdAuthorizationManager`. All other methods are denied. HTTP Basic is enabled for credential exchange. |

Both chains:
- disable CSRF (these are read-only API endpoints consumed by automated tooling)
- use an isolated `UserDetailsService` (see below) so ORD credentials do not bleed into the host application's authentication

### `UserDetailsService` (qualifier: `ordUserDetailsService`)

Built from `ord.credentials` at startup. Each map key becomes a username; the value must be a Spring Security password-encoded string (e.g. `{bcrypt}$2a$12$...`). Registered with `defaultCandidate = false` so it never participates in the host application's authentication.

### `AccessStrategiesResolver`

Resolves the set of access strategies for a given request by extracting the document or resource name from the URL path (`/ord/v1/documents/{name}` or `/ord/v1/resources/{name}`) and looking it up in the appropriate registry. Returns an empty set for any other path.

### `OrdAuthenticationManager` (bean name: `ordBasicAuthenticator`)

The built-in Basic Auth authenticator. Checks whether the request's access strategies include `basic-auth` and, if so, whether the Spring Security `SecurityContextHolder` reports a fully authenticated caller. This is one of potentially many `OrdAuthenticationManager` beans — all of them are collected and injected into `OrdAuthorizationManager` as a `List`.

### `OrdAuthorizationManager`

The access-decision component for the documents and resources filter chains. For each incoming request it:

1. Resolves the access strategies for the request via `AccessStrategiesResolver`.
2. Grants access if **any** of the following holds:
   - The strategies include `open`
   - **Any** registered `OrdAuthenticationManager` bean returns `true` for the request

If the document/resource name is not found in the registry the strategies set is empty and access is denied.

---

## Authentication flow diagram

```
GET /ord/v1/documents/{name}  (or /ord/v1/resources/{name})
        │
        ▼
ordDocumentsSecurityFilterChain  (order HIGHEST_PRECEDENCE + 20)
        │
        ├─ HTTP Basic credentials present? ──► UserDetailsService (ordUserDetailsService)
        │                                       validates against ord.credentials
        │
        ▼
OrdAuthorizationManager.check()
        │
        ├─ AccessStrategiesResolver.resolve()
        │       └─ looks up strategies for {name} in DocumentSchemaRegistry / StaticResourceRegistry
        │
        ├─ strategies contains "open"?                        ──► ALLOW
        ├─ ordBasicAuthenticator.isAuthenticated()?
        │       strategies contain "basic-auth"
        │       AND SecurityContext is fully authenticated?   ──► ALLOW
        ├─ (any other OrdAuthenticationManager bean)?
        │       e.g. CloudFoundryTLSAuthenticator,
        │            KymaTLSAuthenticator, custom bean        ──► ALLOW if returns true
        └─ (none of the above)                                ──► DENY (403)

GET /.well-known/open-resource-discovery
        │
        ▼
ordWellKnownSecurityFilterChain  (order HIGHEST_PRECEDENCE + 10)
        │
        └─ always ALLOW (anonymous())
```

---

## Credential configuration

Generate a bcrypt hash once and store it in `application.yml`. The `{bcrypt}` prefix is required — Spring Security uses it to select the right `PasswordEncoder`.

```java
// Run once to generate the hash:
System.out.println(new BCryptPasswordEncoder().encode("my-password"));
```

```yaml
ord:
  credentials:
    admin: "{bcrypt}$2a$12$te68x8ajPZgD/icO90c0N.N23L0Igd8FN9n0XAv/Al1HFJVAMKoB2"
    reader: "{bcrypt}$2a$10$..."
```

All credentials share a single in-memory store; there is no role distinction between them — any valid credential grants full authenticated access.

---

## Customization

### Add an mTLS authenticator

mTLS is not enabled by default. To activate it, register a configured `CloudFoundryTLSAuthenticator` or `KymaTLSAuthenticator` bean — or any custom `OrdAuthenticationManager` implementation. All registered `OrdAuthenticationManager` beans are collected automatically and evaluated together; registering one does not remove the built-in Basic Auth authenticator.

**SAP Cloud Foundry** — validates client certificates forwarded by the CF router via `X-Forwarded-Client-Cert`, `X-SSL-Client`, `X-SSL-Client-Verify`, `X-SSL-Client-Issuer-DN`, `X-SSL-Client-Subject-DN`, and `X-SSL-Client-Root-CA-DN` headers. Use the fluent `configure(strategy, rootCAs, certs)` method to bind trusted certificates to specific access strategies:

```java
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy;
import org.openresourcediscovery.core.security.OrdAuthenticationManager.TrustedCertificate;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.impl.CloudFoundryTLSAuthenticator;
import java.util.Set;

@Bean
public OrdAuthenticationManager cloudFoundryTlsAuthenticator(
        AccessStrategiesResolver accessStrategiesResolver) {
    return new CloudFoundryTLSAuthenticator(accessStrategiesResolver)
        .configure(
            AccessStrategy.CMP_MTLS,
            Set.of("CN=root-ca,O=MyOrg"),               // trusted root CA DNs
            Set.of(new TrustedCertificate(
                "CN=trusted-issuer,O=MyOrg",             // issuer DN — use "*" to accept any
                "CN=trusted-client,O=MyOrg"              // subject DN — use "*" to accept any
            ))
        );
}
```

**SAP Kyma** — validates via `X-SSL-Client-Issuer` and `X-SSL-Client-CN` headers:

```java
import org.openresourcediscovery.core.security.OrdAuthenticationManager;
import org.openresourcediscovery.core.security.OrdAuthenticationManager.AccessStrategy;
import org.openresourcediscovery.core.security.OrdAuthenticationManager.TrustedCertificate;
import org.openresourcediscovery.core.security.AccessStrategiesResolver;
import org.openresourcediscovery.core.security.impl.KymaTLSAuthenticator;
import java.util.Set;

@Bean
public OrdAuthenticationManager kymaTlsAuthenticator(
        AccessStrategiesResolver accessStrategiesResolver) {
    return new KymaTLSAuthenticator(accessStrategiesResolver)
        .configure(
            AccessStrategy.BAH_MTLS,
            Set.of(new TrustedCertificate(
                "CN=trusted-issuer,O=MyOrg",
                "CN=trusted-client,O=MyOrg"
            ))
        );
}
```

Both implementations support calling `.configure(...)` multiple times to register different certificate sets per access strategy. Certificate DNs are Base64-encoded in the headers and compared order-insensitively (token multiset equality). Use `"*"` as issuer or subject to accept any value.

**Custom** — implement `OrdAuthenticationManager` directly. It receives the raw `HttpServletRequest` and returns `true` to grant access:

```java
@Bean
public OrdAuthenticationManager myCustomAuthenticator() {
    return request -> {
        String token = request.getHeader("X-Internal-Token");
        return token != null && myTokenValidator.isValid(token);
    };
}
```

`OrdAuthenticationManager` is a `@FunctionalInterface`.

### Replace the authorization manager

To change how access decisions are made — for example to add IP allow-listing or a custom claims-based check — provide a bean of type `OrdAuthorizationManager`:

```java
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;

@Bean
public OrdAuthorizationManager ordAuthorizationManager() {
    return (authentication, context) -> {
        // your custom logic here
        boolean granted = /* ... */;
        return new AuthorizationDecision(granted);
    };
}
```

`OrdAuthorizationManager` extends `AuthorizationManager<RequestAuthorizationContext>` and is a functional interface.

### Replace the authentication manager (Basic Auth)

The default Basic Auth authenticator (`ordBasicAuthenticator`) is guarded by `@ConditionalOnMissingBean(name = "ordBasicAuthenticator")`. Override it by declaring a bean with that exact name:

```java
@Bean(name = "ordBasicAuthenticator")
public OrdAuthenticationManager ordBasicAuthenticator() {
    return request -> {
        // custom Basic Auth logic
        return request.getHeader("X-Internal-Token") != null;
    };
}
```

### Replace a filter chain

All three filter chains are guarded by `@ConditionalOnMissingBean(name = "...")`. Declare a bean with the exact name to suppress the default and take full control of that chain:

```java
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.SecurityFilterChain;

// Replace the documents filter chain — e.g. to add JWT support
@Bean(name = "ordDocumentsSecurityFilterChain")
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public SecurityFilterChain ordDocumentsSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/ord/v1/documents/*")
        .csrf(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(GET, "/ord/v1/documents/*").authenticated()
            .anyRequest().denyAll())
        .build();
}
```

Keep the `@Order` value the same as the default — changing it relative to other ORD chains can cause the wrong chain to match first.

---

## Interaction with the host application's security

The ORD filter chains use `@Order(HIGHEST_PRECEDENCE + 10/20/30)` so they match before any application-defined chains. A typical application chain at default order (`Integer.MAX_VALUE`) or lower explicitly-set order will not see requests for ORD paths at all.

The `ordUserDetailsService` bean is registered with `defaultCandidate = false` and a qualified name. It is invisible to Spring Boot's `UserDetailsServiceAutoConfiguration` and to any `@Autowired UserDetailsService` field in the host application — it cannot cause interference.

If the host application already defines a `SecurityFilterChain` that matches `/**` at a higher precedence than `HIGHEST_PRECEDENCE + 10`, it will shadow the ORD chains. Set the host chain's `@Order` to a value higher than `HIGHEST_PRECEDENCE + 30` (i.e. numerically greater) to let the ORD chains take precedence.

---

## Disabling security entirely

```yaml
ord:
  autoconfigure: false
```

This suppresses `OrdSecurityAutoConfiguration` (and `OrdAutoConfiguration`). Both endpoints are taken offline completely. To keep the endpoints active but remove all auth, keep `autoconfigure: true`, remove all `ord.credentials`, and set all document access strategies to `open`.
