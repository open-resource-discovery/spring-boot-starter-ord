# spring-boot-starter-ord

Spring Boot auto-configuration for [Open Resource Discovery (ORD)](https://open-resource-discovery.org). Adds two endpoints to your application:

| Endpoint | Auth | Description |
|---|---|---|
| `GET /.well-known/open-resource-discovery` | None | Lists available ORD documents |
| `GET /ord/v1/documents/{id}` | Basic Auth (or `open`) | Returns an ORD document |

## Installation

```xml
<dependency>
    <groupId>org.open-resource-discovery</groupId>
    <artifactId>spring-boot-starter-ord</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

There are two ways to provide ORD documents: annotation-based generation and static files. Both can be used together.

### Option A — Annotation-based

Annotate your classes with ORD annotations and point the starter at the packages to scan:

```yaml
ord:
  packages:
    - com.example.myapp.resources
```

The starter scans the listed packages, generates an ORD document, and serves it at `/ord/v1/documents/<document-id>` (see: `@Ord.Document` annotation).

### Option B — Static files

Place a pre-built ORD document JSON file on the classpath and declare it:

```yaml
ord:
  documents:
    - id: my-service
      path: classpath:ord/my-document.json
      accessStrategies:
        - open   # omit to require Basic Auth
```

The starter serves the ORD document at `/ord/v1/documents/my-service`.

## Security

Document endpoints require HTTP Basic Auth by default. Define credentials in `application.yml`:

```yaml
ord:
  credentials:
    my-user: {bcrypt}<password-hash>
```

To make a specific document publicly accessible without credentials, set `accessStrategies: [open]` on that document (see Option B above).

The `/.well-known/open-resource-discovery` endpoint is always unauthenticated.

## Visibility filtering

Authenticated requests receive all content (`public` + `internal` + `private`). Unauthenticated requests receive `public` content only. This applies to both annotation-generated and static documents.

## All configuration options

```yaml
ord:
  autoconfigure: true          # set false to disable the starter entirely
  application: ""              # application identifier in the generated document
  namespace: customer          # namespace in the generated document
  packages: []                 # packages to scan for ORD annotations
  documents: []                # static ORD documents to serve
  credentials: {}              # Basic Auth users (username: "{bcrypt}<password-hash>")
```

## License

[Apache License 2.0](LICENSES/Apache-2.0.txt)
