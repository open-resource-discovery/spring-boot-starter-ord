---
name: integrate-ord-starter
description: Integrates Open Resource Discovery (ORD) into Spring Boot applications using spring-boot-starter-ord. Use whenever the user mentions ORD, Open Resource Discovery, exposing service metadata, API catalogues, resource discovery endpoints, or the well-known ORD endpoint — even without the word "starter". Also use when the user asks about configuring ORD documents, using ORD annotations, implementing DocumentSchemaDetector, customizing ORD beans, overriding ORD factory defaults, or securing ORD endpoints with Basic Auth, mTLS, OrdAuthenticationManager, or Spring Security filter chains.
version: 1.6.0
tools: Read, Glob, Grep, Edit, Write, Bash(curl:*), Bash(spring encodepassword:*), Bash(jq:*)
argument-hint: "[describe your goal, e.g. 'add ORD to my service' or 'secure the document endpoint with mTLS']"
---

# Integrate spring-boot-starter-ord

## What is ORD?

Open Resource Discovery (ORD) is a protocol that lets external systems — API gateways, service catalogues, developer portals — automatically discover what a service exposes: its APIs, events, data products, and capabilities. A service publishes this metadata as one or more JSON documents at a well-known URL, and consumers poll that URL to keep their catalogue up to date.

The two-endpoint design reflects this contract:
- The **well-known endpoint** is always unauthenticated so any consumer can find what documents exist and how to access them.
- The **document endpoints** are conditionally authenticated, and visibility filtering means unauthenticated consumers only see entities marked `public` — internal or private APIs are hidden unless the caller authenticates.

`spring-boot-starter-ord` auto-configures both endpoints for a Spring Boot application with no boilerplate beyond a dependency and a few configuration lines.

**Requirements:** Java 17+, Spring Boot 4.x, servlet stack (`spring-boot-starter-web`) — reactive is not supported.

---

## Workflow

Follow these phases in order. Skip a phase only if its outcome is already certain from the user's message.

| Phase | Action |
|---|---|
| **1 — Inspect** | Read the project to find what already exists (see below) |
| **2 — Clarify** | If the approach is ambiguous, ask the user to choose before writing any code |
| **3 — Execute** | Implement only the approach(es) confirmed in Phase 2 — **do not start without user confirmation** |
| **4 — Verify** | Run the curl commands in the Quick reference to confirm both endpoints respond |

---

## Reading the project before starting

**Delegate the entire inspection to the `Explore` subagent** — this keeps file-read output out of the main conversation context and returns only a concise findings summary.

```
Agent(subagent_type="Explore", prompt="""
Inspect this Spring Boot project for existing ORD integration. Report only findings — no code changes.

1. Dependency: search pom.xml, build.gradle, build.gradle.kts for 'spring-boot-starter-ord'
2. ORD config: grep src/main/resources/ for lines starting with 'ord:'
3. Annotations: grep src/main/java/ for '@Ord.Document'
4. Custom detector: grep src/main/java/ for 'DocumentSchemaDetector'

Return a single short summary of what was found for each of the four checks.
""")
```

Use the returned summary to determine which steps are already done and which approach is already in use, then skip straight to the gap.

**What to conclude from the inspection results:**

| What you found | What it means |
|---|---|
| No dependency, no config, no annotations | Greenfield — all three phases apply |
| Dependency present, no config/annotations | Partial setup — dependency step done; go straight to Phase 2 to choose a document approach |
| Dependency + `ord.documents` present | Static JSON already configured — verify it works (Phase 4) or extend it |
| Dependency + `ord.packages` / `@Ord.Document` present | Annotation approach already in use — extend or verify |
| `DocumentSchemaDetector` implementation found | Custom detector already in use — extend or verify |

### Phase 2 — Clarify before acting

**Do not write any code until Phase 2 is complete.** Use `AskUserQuestion` to collect all unknowns in a single message — never ask one question at a time.

If the approach is already unambiguous from the inspection result and the user's opening message, skip straight to Phase 3.

Otherwise call `AskUserQuestion` with whichever questions apply:

- **Approach not yet chosen** (no existing ORD config found, or dependency present but no documents): Ask the user to pick from the "Choose a document approach" decision table — static JSON, Java annotations, or custom `DocumentSchemaDetector`.
- **Dependency missing**: Confirm the build tool (Maven / Gradle / Gradle Kotlin DSL) so the correct dependency snippet can be inserted.
- **Access strategy unclear**: Ask whether the document endpoint should be publicly open, protected by Basic Auth, or mTLS.

Wait for the user's reply before proceeding to Phase 3.

---

## What it provides

| Endpoint | Auth | Purpose |
|---|---|---|
| `GET /.well-known/open-resource-discovery` | None — always public | Lists all documents and their access strategies |
| `GET /ord/v1/documents/{name}` | Conditional | Returns a document; unauthenticated requests receive `public`-visibility entities only |
| `GET /ord/v1/resources/{name}` | Conditional | Serves a static API resource file (e.g. OpenAPI YAML); access controlled per `ord.api-resources[].accessStrategies` |

---

## Add the dependency

**Check `references/versions.md` for the current version before inserting any snippet.** Look up the latest release on [Maven Central](https://central.sonatype.com/artifact/org.open-resource-discovery/spring-boot-starter-ord) if the ledger entry is stale.

**Maven:**
```xml
<dependency>
    <groupId>org.open-resource-discovery</groupId>
    <artifactId>spring-boot-starter-ord</artifactId>
    <version><!-- see references/versions.md --></version>
</dependency>
```

**Gradle:**
```groovy
implementation 'org.open-resource-discovery:spring-boot-starter-ord:<!-- see references/versions.md -->'
```

**Gradle Kotlin DSL:**
```kotlin
implementation("org.open-resource-discovery:spring-boot-starter-ord:<!-- see references/versions.md -->")
```

---

## Choose a document approach

All three approaches can coexist. The `DocumentSchemaRegistry` is populated by collecting **all** `DocumentSchemaDetector` beans — each approach contributes its own bean and their results are merged.

| Signal in the project | Approach to use |
|---|---|
| ORD JSON files exist in `src/main/resources/` | **Static JSON** — declare them under `ord.documents` |
| Classes annotated with `@Ord.Document` exist, or `ord.packages` is set | **Java annotations** — add the package to `ord.packages` and annotate |
| Neither exists and documents depend on runtime data (DB, external API) | **Custom `DocumentSchemaDetector`** — implement the interface and register a bean |

---

## Static JSON documents

Place the file on the classpath (e.g. `src/main/resources/ord/document.json`).

**Before writing the file, check `references/versions.md` for the current `openResourceDiscovery` version string and `$schema` URL — both are version-sensitive.**

```json
{
  "$schema": "<!-- see references/versions.md for current URL -->",
  "openResourceDiscovery": "<!-- see references/versions.md -->",
  "description": "ORD document for My Service",
  "packages": [],
  "apiResources": []
}
```

Declare it in `application.yml`:

```yaml
ord:
  documents:
    - name: my-service             # URL: /ord/v1/documents/my-service
      path: classpath:ord/document.json
      accessStrategies:
        - open                   # omit to require Basic Auth instead
```

Multiple documents can be declared; each gets its own `{name}`. For the full `ord.documents` property schema and defaults, see `references/configuration.md`.

---

## Static API resources

Place the file on the classpath (e.g. `src/main/resources/ord/my-api.yaml`).

Declare it in `application.yml`:

```yaml
ord:
  api-resources:
    - name: my-api                             # URL: /ord/v1/resources/my-api
      path: classpath:ord/my-api.yaml
      mediaType: application/yaml
      accessStrategies:
        - open                                 # omit to require Basic Auth instead
```

Each entry is served at `/ord/v1/resources/{name}` with the declared `Content-Type`. Multiple entries can be declared. For the full `ord.api-resources` property schema, see `references/configuration.md`.

---

## Java annotations

Add the package to scan in `application.yml`. Use `ord.namespace` to set your organisation's namespace prefix for generated ordIds (default: `"customer"`). For all `ord.packages` and `ord.namespace` options, see `references/configuration.md`.

```yaml
ord:
  packages:
    - com.example.myapp.ord
```

Create a plain class — the class serves solely as a container for ORD annotation declarations; it is never instantiated by the application. Annotate it with `@Ord.Document` and add `@Ord.Package`, `@Ord.ApiResource`, etc. on methods within it:

### Overriding access strategies from application.yml

The access strategies declared on `@Ord.Document` can be overridden without changing source code by adding an `ord.documents` entry with the same `name` and **no `path`**:

```yaml
ord:
  documents:
    - name: my-service        # matches @Ord.Document(name = "my-service")
      accessStrategies:
        - sap:cmp-mtls:v1    # replaces whatever the annotation declares
```

When a matching entry is found (same `name`, empty `path`), its `accessStrategies` takes precedence. When no matching entry exists, the strategies from the annotation are used. This allows deployment-time configuration of access strategies without recompiling.

```java
import org.openresourcediscovery.annotations.Ord;

@Ord.Document(id = "my-service")
public class MyServiceOrdDocument {

    @Ord.Package(
        ordId = "mycompany:package:apis:v1",
        title = "My Service APIs",
        partOfDocument = @Ord.Package.PartOfDocument(id = "my-service")
    )
    public void apiPackage() {}

    @Ord.ApiResource(
        ordId = "mycompany:apiResource:orders:v1",
        title = "Orders API",
        partOfPackage = @Ord.ApiResource.PartOfPackage(id = "mycompany:package:apis:v1")
    )
    public void ordersApi() {}
}
```

**All supported annotation types** (in `@Ord.*`, processed in this order):
`Vendor` → `SystemType` → `SystemVersion` → `SystemInstance` → `ConsumptionBundle` → `Product` → `Package` → `GroupType` → `Group` → `Tombstone` → `Agent` → `EntityType` → `Capability` → `DataProduct` → `ApiResource` → `EventResource` → `Overlay` → `IntegrationDependency`

Processing order matters because later entity types reference earlier ones — for example, an `ApiResource` must reference a `Package` ordId, so `Package` is processed first. Declare annotations in any order; the scanner enforces the sequence internally.

---

## Custom `DocumentSchemaDetector`

`ServicesConfiguration` injects all `DocumentSchemaDetector` beans as a `Collection` and folds their results into the registry. Registering a custom bean is all that is required — no additional wiring.

**Implement the interface:**

```java
import java.util.Map;
import java.util.Set;
import org.openresourcediscovery.core.configurations.properties.OrdProperties;
import org.openresourcediscovery.core.services.DocumentSchemaDetector;
import org.openresourcediscovery.model.DocumentSchema;

public class MyCustomDocumentSchemaDetector implements DocumentSchemaDetector {

    private final MyDocumentSource source;

    public MyCustomDocumentSchemaDetector(MyDocumentSource source) {
        this.source = source;
    }

    @Override
    public Map<String, DetectionResult> detect(OrdProperties properties) {
        DocumentSchema schema = source.load();
        return Map.of(
            "my-service",                        // id → /ord/v1/documents/my-service
            new DetectionResult(schema, Set.of("open"))
        );
    }
}
```

**Return type:** `Map<String, DetectionResult>`
- Key — document name (the URL path segment)
- `DetectionResult.document()` — the `DocumentSchema` object
- `DetectionResult.strategies()` — access strategy names: `"open"`, `"basic-auth"`, or `"sap:cmp-mtls:v1"`

**Register as a Spring bean:**

```java
@Configuration
public class OrdConfiguration {

    @Bean
    public DocumentSchemaDetector myCustomDocumentSchemaDetector(MyDocumentSource source) {
        return new MyCustomDocumentSchemaDetector(source);
    }
}
```

`DocumentSchemaDetector` is a functional interface — a lambda works when no injected fields are needed.

**Constraints:**
- `detect()` is called **once at startup**; results are cached for the application lifetime. For live updates, provide a custom `DocumentSchemaRegistry` bean — the auto-configured one is guarded by `@ConditionalOnMissingBean`.
- Id collisions across detectors: for single-entry detectors the later-registered bean wins because `register()` overwrites. When a detector returns multiple documents (e.g. via `Map.of(...)`), the encounter order within that map is unspecified in Java, so collision outcomes are non-deterministic — avoid duplicate ids across detectors entirely.
- `OrdProperties` in `detect()` gives access to `properties.getNamespace()` and `properties.getApplication()` without an extra constructor dependency.

For building `DocumentSchema` objects in code, returning multiple documents from one detector, and a full dynamic multi-tenant example, see `references/custom-detector.md`.

---

## Security and credentials

### Access strategies

| Strategy | Behaviour                          |
|---|------------------------------------|
| `open` | No auth required                   |
| `basic-auth` *(default when omitted)* | HTTP Basic Auth required           |
| `sap:cmp-mtls:v1` | SAP Cloud Management Platform mTLS |
| `sap.businesshub:mtls:v1` | SAP Business Accelerator Hub mTLS  |

A document can declare multiple strategies; the caller picks one.

### Visibility filtering

Unauthenticated requests (open access) return only entities with `"visibility": "public"`. Authenticated requests return all visibility levels (`public`, `internal`, `private`).

### Basic Auth credentials

```java
// Generate a bcrypt hash once:
System.out.println(new BCryptPasswordEncoder().encode("my-password"));
```

```yaml
ord:
  credentials:
    admin: "{bcrypt}$2a$12$..."    # admin:my-password — the {bcrypt} prefix is required
    reader: "{bcrypt}$2a$10$..."
```

### Disable the starter

```yaml
ord:
  autoconfigure: false
```

For the full `ord.*` property schema, all defaults, and per-property Spring condition details, see `references/configuration.md`. For how the Spring Security filter chains are wired, how access decisions are made, and how to replace any security bean (`OrdAuthenticationManager`, `OrdAuthorizationManager`, a filter chain), see `references/security.md`. To override individual generators, processors, or other default beans, see `references/customization.md`.

---

## Quick reference

### Verify the endpoints

```bash
# Well-known — always public
curl http://localhost:8080/.well-known/open-resource-discovery

# Document — open access
curl http://localhost:8080/ord/v1/documents/my-service

# Document — Basic Auth
curl -u admin:my-password http://localhost:8080/ord/v1/documents/my-service
```

**Expected responses:**
- Well-known: `200` with a JSON body under the key `"openResourceDiscoveryV1"`, containing a `"documents"` array. Each entry has `"url"`, `"perspective"` (default `"system-instance"`), and `"accessStrategies"`.
- Document endpoint: `200` with the ORD document JSON. If using Basic Auth and credentials are wrong, expect `401`. If the id in the URL does not match any configured document, expect `404`.

**If verification fails, check the Common pitfalls table below before investigating further.**

### Common pitfalls

| Symptom | Cause | Fix |
|---|---|---|
| 404 on document endpoint | `name` mismatch between URL and config/annotation | Match names exactly |
| 401 on every request | No `open` strategy, no credentials supplied | Add `accessStrategies: [open]` or pass `-u user:pass` |
| Endpoints not registered | Missing `spring-boot-starter-web`, or `ord.autoconfigure: false` | Add web starter; check config |
| Annotations ignored | Package not listed under `ord.packages` | Add the correct package name |
| Empty document returned | Visibility filtering: no `public` entities | Authenticate, or mark entities `"visibility": "public"` |
| Startup error with credentials | Missing `{bcrypt}` prefix | Use `"{bcrypt}$2a$..."` format |
| Custom detector not called | Bean not in Spring context | Annotate with `@Bean` inside a `@Configuration` class |

---

## Reference files

- `references/custom-detector.md` — Load when building `DocumentSchema` objects in code, returning multiple documents from one detector, or implementing a dynamic multi-tenant detector
- `references/customization.md` — Load when the user wants to override a default bean, replace a specific generator or processor, add mTLS support, or use the `EntityGeneratorFactoryCustomizer` / `AnnotationProcessorFactoryCustomizer` SPIs
- `references/security.md` — Load when the user asks how Spring Security is configured, how the filter chains work, how access decisions are made, how to configure Basic Auth credentials, how to enable mTLS, or how to replace any security bean (`OrdAuthenticationManager`, `OrdAuthorizationManager`, a filter chain)
- `references/configuration.md` — Load when looking up the full `ord.*` property schema, defaults, and per-property Spring condition details
- `references/versions.md` — Version-sensitive claims ledger; check here before inserting dependency versions or ORD schema version strings

---

## External documentation

- [Open Resource Discovery specification](https://open-resource-discovery.org/spec-v1) — Load when the user asks about ORD document field semantics, `ordId` format rules, visibility levels, access strategies, or the ORD protocol itself
- [ORD Document API (OAS)](https://open-resource-discovery.org/spec-v1/interfaces/document-api?_highlight=document&_highlight=api) — Load when the user asks about the structure of the ORD document API, request/response schemas, or the OpenAPI specification for the ORD endpoints
- [SAP CMP mTLS access strategy](https://open-resource-discovery.org/spec-extensions/access-strategies/sap-cmp-mtls-v1) — Load when the user asks about the `sap:cmp-mtls:v1` access strategy, how mTLS authentication works for ORD endpoints, or certificate requirements
