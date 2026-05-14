# Full `ord.*` Configuration Reference

All properties under the `ord` prefix in `application.yml` / `application.properties`.

**Contents:** [Full YAML overview](#full-yaml-overview) · [Property details](#property-details) · [Generating a bcrypt password hash](#generating-a-bcrypt-password-hash)

## Full YAML overview

```yaml
ord:
  # Master switch. Set to false to disable all ORD endpoints entirely.
  # Default: true
  autoconfigure: true

  # Base path for the document endpoints. Default: /ord/v1/documents
  api-base-path-documents: /ord/v1/documents

  # Base path for the static resource endpoints. Default: /ord/v1/resources
  api-base-path-resources: /ord/v1/resources

  # Path of the well-known discovery endpoint. Default: /.well-known/open-resource-discovery
  api-base-path-well-known: /.well-known/open-resource-discovery

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
  # Each entry is exposed at /ord/v1/documents/{name}.
  documents:
    - name: my-service                       # URL path segment (required, must be unique)
      path: classpath:ord/document.json     # Spring resource path (required)
      accessStrategies:                     # Who can fetch this document
        - open                              # "open" | "basic-auth" | "sap:cmp-mtls:v1"
                                            # Omit list to default to basic-auth only
    - name: my-service-internal
      path: classpath:ord/internal.json
      # No accessStrategies → defaults to basic-auth

  # Static API resource files to serve.
  # Each entry is exposed at /ord/v1/resources/{name}.
  api-resources:
    - name: my-api                           # URL path segment (required, must be unique)
      path: classpath:ord/my-api.yaml       # Spring resource path (required)
      mediaType: application/yaml           # Content-Type returned (required)
      accessStrategies:                     # Who can fetch this resource
        - open                              # "open" | "basic-auth" | "sap:cmp-mtls:v1"
                                            # Omit list to default to basic-auth only

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

### `ord.api-base-path-documents`
- **Type:** `String`
- **Default:** `"/ord/v1/documents"`
- Base URL prefix for the document endpoints. Each document is served at `{api-base-path-documents}/{name}`. Configurable via `@GetMapping("${ord.api-base-path-documents:/ord/v1/documents}/{name}")`.

### `ord.api-base-path-resources`
- **Type:** `String`
- **Default:** `"/ord/v1/resources"`
- Base URL prefix for the static API resource endpoints.

### `ord.api-base-path-well-known`
- **Type:** `String`
- **Default:** `"/.well-known/open-resource-discovery"`
- Path of the discovery endpoint.

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

### `ord.documents[].name`
- **Type:** `String` (required)
- Becomes the URL path segment: `/ord/v1/documents/{name}`. Must be unique across all declared documents.

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
- Only relevant when at least one document or API resource uses `basic-auth` access strategy.

### `ord.api-resources`
- **Type:** `List<OrdProperties.ApiResource>`
- **Default:** `[]` (empty — `/ord/v1/resources/*` endpoint inactive until non-empty)
- Each entry registers a static file at `/ord/v1/resources/{name}`.

### `ord.api-resources[].name`
- **Type:** `String` (required)
- Becomes the URL path segment: `/ord/v1/resources/{name}`. Must be unique across all declared API resources.

### `ord.api-resources[].path`
- **Type:** `String` (required)
- Any Spring `Resource` path: `classpath:ord/my-api.yaml`, `file:/etc/ord/my-api.json`, etc.
- Served verbatim with the declared `mediaType`.

### `ord.api-resources[].mediaType`
- **Type:** `String` (required)
- The `Content-Type` returned with the file (e.g. `application/yaml`, `application/json`).

### `ord.api-resources[].accessStrategies`
- **Type:** `Set<String>`
- **Default:** `[]` → treated as `basic-auth` only
- Supported values: `"open"`, `"basic-auth"`, `"sap:cmp-mtls:v1"`
- Multiple strategies can be declared; the caller picks one.

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
