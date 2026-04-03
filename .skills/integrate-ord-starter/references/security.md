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

Both chains:
- disable CSRF (these are read-only API endpoints consumed by automated tooling)
- use an isolated `UserDetailsService` (see below) so ORD credentials do not bleed into the host application's authentication

### `UserDetailsService` (qualifier: `ordUserDetailsService`)

Built from `ord.credentials` at startup. Each map key becomes a username; the value must be a Spring Security password-encoded string (e.g. `{bcrypt}$2a$12$...`). Registered with `defaultCandidate = false` so it never participates in the host application's authentication.

### `AuthenticationTrustResolver` (qualifier: `ordAuthenticationTrustResolver`)

A standard `AuthenticationTrustResolverImpl` instance. Also registered with `defaultCandidate = false` to avoid polluting the application context.

### `OrdAuthenticationManager`

Used internally to answer "is the current caller authenticated?" — combines the result of the configured `TLSAuthenticator` (mTLS check) with the Spring Security `SecurityContextHolder` state (Basic Auth check). Injected into controllers that need to determine authenticated-vs-anonymous for visibility filtering.

### `OrdAuthorizationManager`

The access-decision component for the documents filter chain. For each incoming request it:

1. Extracts the document `{id}` from the URL path.
2. Looks up the access strategies registered for that document in the `DocumentSchemaRegistry`.
3. Grants access if **any** of the following holds:
   - The document has strategy `open`
   - The document has strategy `sap:cmp-mtls:v1` **and** `TLSAuthenticator.isAuthenticated()` returns `true`
   - The document has strategy `basic-auth` **and** Spring Security reports the caller as authenticated

If the document id is not found in the registry the strategies set is empty and access is denied.

---

## Authentication flow diagram

```
GET /ord/v1/documents/{id}
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
        ├─ strategies contains "open"?               ──► ALLOW
        ├─ strategies contains "sap:cmp-mtls:v1"
        │    AND TLSAuthenticator.isAuthenticated()?  ──► ALLOW
        ├─ strategies contains "basic-auth"
        │    AND SecurityContext.isAuthenticated()?   ──► ALLOW
        └─ (none of the above)                        ──► DENY (403)

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

### Replace the TLS authenticator

The default `TLSAuthenticator` always returns `false`. To enable mTLS, replace it with one of the two built-in implementations or a custom one:

**SAP Cloud Foundry** — validates client certificates forwarded by the CF router via `X-Forwarded-Client-Cert`, `X-SSL-Client`, `X-SSL-Client-Verify`, `X-SSL-Client-Issuer-DN`, `X-SSL-Client-Subject-DN`, and `X-SSL-Client-Root-CA-DN` headers:

```java
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.openresourcediscovery.core.security.impl.CloudFoundryTLSAuthenticator;
import java.util.Set;

@Bean
public TLSAuthenticator tlsAuthenticator() {
    return new CloudFoundryTLSAuthenticator(
        Set.of(new TLSAuthenticator.TrustedCertificate(
            "CN=trusted-issuer,O=MyOrg",   // issuer DN — use "*" to accept any
            "CN=trusted-client,O=MyOrg"    // subject DN — use "*" to accept any
        )),
        Set.of("CN=root-ca,O=MyOrg")       // trusted root CA DNs — use "*" to accept any
    );
}
```

**SAP Kyma** — validates via `X-SSL-Client-Issuer` and `X-SSL-Client-CN` headers:

```java
@Bean
public TLSAuthenticator tlsAuthenticator() {
    return new KymaTLSAuthenticator(
        Set.of(new TLSAuthenticator.TrustedCertificate(
            "CN=trusted-issuer,O=MyOrg",
            "CN=trusted-client,O=MyOrg"
        ))
    );
}
```

**Custom** — implement `TLSAuthenticator` directly:

```java
@Bean
public TLSAuthenticator tlsAuthenticator() {
    return request -> {
        X509Certificate[] certs =
            (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        return certs != null && myTrustStore.isTrusted(certs[0]);
    };
}
```

`TLSAuthenticator` is a `@FunctionalInterface`, so a lambda is sufficient for simple cases.

### Replace the authorization manager

To change how access decisions are made for the documents endpoint — for example to add IP allow-listing or a custom claims-based check — provide a bean of type `OrdAuthorizationManager`:

```java
import org.openresourcediscovery.core.security.OrdAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;

@Bean
public OrdAuthorizationManager ordAuthorizationManager(
        DocumentSchemaRegistry registry,
        TLSAuthenticator tlsAuthenticator) {
    return (authentication, context) -> {
        // your custom logic here
        boolean granted = /* ... */;
        return new AuthorizationDecision(granted);
    };
}
```

`OrdAuthorizationManager` extends `AuthorizationManager<RequestAuthorizationContext>` and is a functional interface.

### Replace the authentication manager

`OrdAuthenticationManager` is used by the controllers to determine the caller's authenticated state for visibility filtering (not for access control — that is `OrdAuthorizationManager`). Replace it when you need custom authenticated-vs-anonymous semantics:

```java
import org.openresourcediscovery.core.security.OrdAuthenticationManager;

@Bean
public OrdAuthenticationManager ordAuthenticationManager() {
    return request -> {
        // return true if the caller should receive internal/private entities
        return request.getHeader("X-Internal-Token") != null;
    };
}
```

`OrdAuthenticationManager` is a `@FunctionalInterface`.

### Replace a filter chain

Both filter chains are guarded by `@ConditionalOnMissingBean(name = "...")`. Declare a bean with the exact name to suppress the default and take full control of that chain:

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

### Use separate credentials per document

The default `UserDetailsService` applies the same credential store to all documents. If you need per-document credentials, replace `OrdAuthorizationManager` and `OrdAuthenticationManager` together with a custom implementation that reads credentials from a document-specific source.

---

## Interaction with the host application's security

The ORD filter chains use `@Order(HIGHEST_PRECEDENCE + 10/20)` so they match before any application-defined chains. A typical application chain at default order (`Integer.MAX_VALUE`) or lower explicitly-set order will not see requests for ORD paths at all.

The `ordUserDetailsService` and `ordAuthenticationTrustResolver` beans are registered with `defaultCandidate = false` and qualified names. They are invisible to Spring Boot's `UserDetailsServiceAutoConfiguration` and to any `@Autowired AuthenticationTrustResolver` field in the host application — they cannot cause interference.

If the host application already defines a `SecurityFilterChain` that matches `/**` at a higher precedence than `HIGHEST_PRECEDENCE + 10`, it will shadow the ORD chains. Set the host chain's `@Order` to a value higher than `HIGHEST_PRECEDENCE + 20` (i.e. numerically greater) to let the ORD chains take precedence.

---

## Disabling security entirely

```yaml
ord:
  autoconfigure: false
```

This suppresses `OrdSecurityAutoConfiguration` (and `OrdAutoConfiguration`). Both endpoints are taken offline completely. To keep the endpoints active but remove all auth, keep `autoconfigure: true`, remove all `ord.credentials`, and set all document access strategies to `open`.
