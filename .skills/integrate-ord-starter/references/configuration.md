# Full `ord.*` Configuration Reference

All properties under the `ord` prefix in `application.yml` / `application.properties`.

**Contents:** [Full YAML overview](#full-yaml-overview) · [Property details](#property-details) · [Generating a bcrypt password hash](#generating-a-bcrypt-password-hash)

## Full YAML overview

```yaml
ord:
  # Master switch. Set to false to disable all ORD endpoints entirely.
  # Default: true
  autoconfigure: true

  # Application identifier embedded in generated documents.
  # Default: "" (empty string)
  application: my-service

  # Namespace used when constructing ordIds in generated documents.
  # Default: "customer"
  namespace: mycompany

  # Java packages to scan for ORD annotations (@Ord.Document, @Ord.Package, etc.).
  # Required for the annotation-based approach; omit if using only static documents.
  packages:
    - com.example.myapp.ord
    - com.example.myapp.events

  # Static ORD JSON documents to serve.
  # Each entry is exposed at /ord/v1/documents/{id}.
  documents:
    - id: my-service                        # URL path segment (required, must be unique)
      path: classpath:ord/document.json     # Spring resource path (required)
      accessStrategies:                     # Who can fetch this document
        - open                              # "open" | "basic-auth" | "sap:cmp-mtls:v1"
                                            # Omit list to default to basic-auth only
    - id: my-service-internal
      path: classpath:ord/internal.json
      # No accessStrategies → defaults to basic-auth

  # HTTP Basic Auth credentials.
  # Key = username, value = bcrypt-encoded password with {bcrypt} prefix.
  credentials:
    admin: "{bcrypt}$2a$12$te68x8ajPZgD/icO90c0N.N23L0Igd8FN9n0XAv/Al1HFJVAMKoB2"
    reader: "{bcrypt}$2a$10$..."
```

## Property details

### `ord.autoconfigure`
- **Type:** `Boolean`
- **Default:** `true`
- Controlled by `@ConditionalOnBooleanProperty(name = "ord.autoconfigure", matchIfMissing = true)`.
- Set to `false` to suppress all ORD beans and endpoints without removing the dependency.

### `ord.application`
- **Type:** `String`
- **Default:** `""` (empty string)
- Placed in the `application` field of auto-generated documents.

### `ord.namespace`
- **Type:** `String`
- **Default:** `"customer"`
- Used as the prefix when the annotation processor constructs ordIds (e.g. `customer:package:default:v1`). Override with your organisation's namespace.

### `ord.packages`
- **Type:** `List<String>`
- **Default:** `[]` (empty — annotation scanning disabled)
- Enables `JavaAnnotationsDocumentSchemaDetector`. Scanned once at startup; results cached by `CachingOrdAnnotationsScannerImpl`.

### `ord.documents[].id`
- **Type:** `String` (required)
- Becomes the URL path segment: `/ord/v1/documents/{id}`. Must be unique across all declared documents.

### `ord.documents[].path`
- **Type:** `String` (required)
- Any Spring `Resource` path: `classpath:ord/doc.json`, `file:/etc/ord/doc.json`, etc.
- Loaded and deserialised to `DocumentSchema` at startup.

### `ord.documents[].accessStrategies`
- **Type:** `Set<String>`
- **Default:** `[]` → treated as `basic-auth` only
- Supported values: `"open"`, `"basic-auth"`, `"sap:cmp-mtls:v1"`
- Multiple strategies are listed in the well-known response; the caller picks one.

### `ord.credentials`
- **Type:** `Map<String, String>` — username → encoded password
- Passwords **must** use Spring Security's delegating encoder format: `{bcrypt}<hash>`.
- Loaded into `InMemoryUserDetailsManager` at startup.
- Only relevant when at least one document uses `basic-auth` access strategy.

## Generating a bcrypt password hash

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashUtil {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("my-password"));
        // prints: $2a$10$...
        // Prepend {bcrypt} before storing in application.yml
    }
}
```

Or via the Spring Boot CLI:
```bash
spring encodepassword my-password
```
