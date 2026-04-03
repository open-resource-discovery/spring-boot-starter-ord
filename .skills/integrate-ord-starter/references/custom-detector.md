# Custom DocumentSchemaDetector — Reference

Detailed patterns for the custom `DocumentSchemaDetector` approach in the integrate-ord-starter skill.

**Contents:** [Building a DocumentSchema programmatically](#building-a-documentschema-programmatically) · [Returning multiple documents](#returning-multiple-documents-from-one-detector) · [Using OrdProperties inside detect()](#using-ordproperties-inside-detect) · [Combining with static JSON](#combining-with-static-json-deserialisation-helper) · [Full example](#full-example-with-all-patterns-combined)

---

## Building a `DocumentSchema` programmatically

Use the model's fluent builder API when constructing documents in code rather than deserialising JSON:

```java
import java.util.List;
import org.openresourcediscovery.model.ApiResource;
import org.openresourcediscovery.model.DocumentSchema;
import org.openresourcediscovery.model.Package;

DocumentSchema schema = new DocumentSchema()
    .withOpenResourceDiscovery("<!-- see references/versions.md -->")
    .withDescription("ORD document for My Service")
    .withPackages(List.of(
        new Package()
            .withOrdId("mycompany:package:apis:v1")
            .withTitle("My Service APIs")
            .withVersion("1.0.0")
            .withVendor("customer:vendor:Customer:")
            .withShortDescription("Short description")
    ))
    .withApiResources(List.of(
        new ApiResource()
            .withOrdId("mycompany:apiResource:orders:v1")
            .withTitle("Orders API")
            .withVersion("1.0.0")
            .withPartOfPackage("mycompany:package:apis:v1")
    ));
```

All model classes follow the same `withXxx(...)` pattern and mirror the ORD JSON schema fields directly.

---

## Returning multiple documents from one detector

A single `DocumentSchemaDetector` bean can register any number of documents:

```java
@Override
public Map<String, Pair<DocumentSchema, Set<String>>> detect(OrdProperties properties) {
    return Map.of(
        "service-apis",    ImmutablePair.of(apisSchema,    Set.of("basic-auth")),
        "service-events",  ImmutablePair.of(eventsSchema,  Set.of("open")),
        "service-internal", ImmutablePair.of(internalSchema, Set.of("basic-auth"))
    );
}
```

Each entry becomes its own endpoint: `/ord/v1/documents/service-apis`, etc.

---

## Using `OrdProperties` inside `detect()`

`OrdProperties` is passed directly into `detect()` so configuration values are available without an extra constructor dependency:

```java
@Override
public Map<String, Pair<DocumentSchema, Set<String>>> detect(OrdProperties properties) {
    String namespace = properties.getNamespace();     // e.g. "customer"
    String application = properties.getApplication(); // e.g. "my-service"

    DocumentSchema schema = new DocumentSchema()
        .withOpenResourceDiscovery("<!-- see references/versions.md -->")
        .withDescription("ORD document for " + application);

    // Use namespace to construct ordIds consistently:
    // e.g. namespace + ":package:apis:v1"

    return Map.of("my-service", ImmutablePair.of(schema, Set.of("open")));
}
```

---

## Combining with static JSON (deserialisation helper)

To load a JSON file inside a custom detector (instead of using `ord.documents` config):

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

public class MyCustomDocumentSchemaDetector implements DocumentSchemaDetector {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    // constructor injection

    @Override
    @SneakyThrows
    public Map<String, Pair<DocumentSchema, Set<String>>> detect(OrdProperties properties) {
        String json = resourceLoader
            .getResource("classpath:ord/my-document.json")
            .getContentAsString(StandardCharsets.UTF_8);

        DocumentSchema schema = objectMapper.readValue(json, DocumentSchema.class);
        return Map.of("my-service", ImmutablePair.of(schema, Set.of("open")));
    }
}
```

This replicates what `StaticFileDocumentSchemaDetector` does internally — useful when the file path or access strategy must be resolved dynamically.

---

## Full example with all patterns combined

```java
@Configuration
public class OrdConfiguration {

    @Bean
    public DocumentSchemaDetector dynamicOrdDetector(MyDocumentRepository repo) {
        return ordProperties -> {
            // Build one document per tenant from the database
            return repo.findAll().stream()
                .collect(Collectors.toMap(
                    tenant -> "tenant-" + tenant.getId(),
                    tenant -> ImmutablePair.of(
                        buildSchema(tenant, ordProperties),
                        Set.of("basic-auth")
                    )
                ));
        };
    }

    private DocumentSchema buildSchema(Tenant tenant, OrdProperties properties) {
        return new DocumentSchema()
            .withOpenResourceDiscovery("<!-- see references/versions.md -->")
            .withDescription("ORD document for tenant " + tenant.getName())
            .withPackages(List.of(
                new Package()
                    .withOrdId(properties.getNamespace() + ":package:" + tenant.getId() + ":v1")
                    .withTitle(tenant.getName() + " APIs")
                    .withVersion("1.0.0")
                    .withVendor("customer:vendor:Customer:")
            ));
    }
}
```

Note: the lambda form `ordProperties -> { ... }` works because `DocumentSchemaDetector` is a functional interface.
