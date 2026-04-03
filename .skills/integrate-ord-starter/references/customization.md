# Customizing the ORD Starter — Reference

Patterns for overriding default beans and fine-tuning the factory pipelines without replacing them entirely.

**Contents:** [Two levels of customization](#two-levels-of-customization) · [Fine-grained: factory customizers](#fine-grained-factory-customizers) · [Coarse-grained: replacing entire beans](#coarse-grained-replacing-entire-beans) · [Decision guide](#decision-guide)

---

## Two levels of customization

| Level | Mechanism | When to use |
|---|---|---|
| **Fine-grained** | `EntityGeneratorFactoryCustomizer` / `AnnotationProcessorFactoryCustomizer` | Replace or add a single generator/processor while keeping all defaults |
| **Coarse-grained** | `@ConditionalOnMissingBean` override | Replace an entire bean (factory, registry, scanner, authenticator, detector) |

---

## Fine-grained: factory customizers

Both factory beans (`EntityGeneratorFactory` and `AnnotationProcessorFactory`) accept an optional `*Customizer` bean that receives the builder before it is finalised. Register exactly one `@Bean` of the customizer type — the factory picks it up automatically via `Optional<...>` injection.

### Replace one generator

Override the generator for a single annotation type while keeping all others at their defaults:

```java
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.services.EntityGeneratorFactory.EntityGeneratorFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdCustomizationConfig {

    @Bean
    public EntityGeneratorFactoryCustomizer entityGeneratorFactoryCustomizer() {
        return builder -> builder
            .withSupplier(Ord.ApiResource.class, MyApiResourceGenerator::new);
    }
}
```

`MyApiResourceGenerator` must implement `EntityGenerator<Ord.ApiResource, ApiResource>`. It will be autowired by the factory immediately after construction, so `@Resource`-annotated fields are populated before first use.

### Replace one processor

```java
import org.openresourcediscovery.annotations.Ord;
import org.openresourcediscovery.core.services.AnnotationProcessorFactory.AnnotationProcessorFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdCustomizationConfig {

    @Bean
    public AnnotationProcessorFactoryCustomizer annotationProcessorFactoryCustomizer() {
        return builder -> builder
            .withSupplier(Ord.Package.class, MyPackageAnnotationProcessor::new);
    }
}
```

### Replace the entire supplier map

Use `withSuppliers(map)` to discard all defaults and provide a fully custom set:

```java
@Bean
public EntityGeneratorFactoryCustomizer entityGeneratorFactoryCustomizer() {
    return builder -> builder.withSuppliers(Map.of(
        Ord.ApiResource.class, MyApiResourceGenerator::new,
        Ord.Package.class,     MyPackageGenerator::new
    ));
}
```

> **Caution:** `withSuppliers` replaces the entire map. Any annotation type not present in the new map will throw `NullPointerException` at runtime when `create()` is called for it. Only use this when you are intentionally restricting the set of supported annotations.

### Combine multiple overrides

Chain multiple `withSupplier` calls:

```java
@Bean
public EntityGeneratorFactoryCustomizer entityGeneratorFactoryCustomizer() {
    return builder -> builder
        .withSupplier(Ord.ApiResource.class,  MyApiResourceGenerator::new)
        .withSupplier(Ord.EventResource.class, MyEventResourceGenerator::new);
}
```

---

## Coarse-grained: replacing entire beans

Most beans in `ServicesConfiguration` are guarded by `@ConditionalOnMissingBean` (type-based). The two security-internal beans — `ordAuthenticationTrustResolver` and `ordUserDetailsService` — are guarded by `@ConditionalOnMissingBean(name = "...")`. To override them, register a bean with that exact name; registering a bean of the same type alone is not sufficient.

### Replace the TLS authenticator

The default `TLSAuthenticator` always returns `false` (no mTLS). Replace it to validate client certificates:

```java
import jakarta.servlet.http.HttpServletRequest;
import org.openresourcediscovery.core.security.TLSAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdCustomizationConfig {

    @Bean
    public TLSAuthenticator tlsAuthenticator() {
        return request -> {
            X509Certificate[] certs =
                (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            return certs != null && isValidCert(certs[0]);
        };
    }
}
```

Two built-in implementations are provided if you are on SAP infrastructure:
- `CloudFoundryTLSAuthenticator` — validates the SAP Cloud Foundry client certificate header
- `KymaTLSAuthenticator` — validates the SAP Kyma client certificate header

Instantiate and register either as a `@Bean` to activate it.

### Replace the document schema registry

The default registry caches documents once at startup and applies visibility filtering on every read. Replace it when you need live reloading or a different storage backend:

```java
import org.openresourcediscovery.core.services.DocumentSchemaRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdCustomizationConfig {

    @Bean
    public DocumentSchemaRegistry documentSchemaRegistry(/* your deps */) {
        return new MyLiveReloadingRegistry(/* ... */);
    }
}
```

`MyLiveReloadingRegistry` must implement `DocumentSchemaRegistry`. The auto-configured one (`DocumentSchemaRegistryImpl`) is skipped entirely.

### Replace the annotation scanner

The default scanner (`CachingOrdAnnotationsScannerImpl`) scans configured packages once at `@PostConstruct` and caches results. To provide a custom scanning strategy:

```java
import org.openresourcediscovery.core.services.OrdAnnotationsScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdCustomizationConfig {

    @Bean
    public OrdAnnotationsScanner ordAnnotationsScanner() {
        return new MyAnnotationsScanner();
    }
}
```

### Suppress a built-in detector

To disable the static-file or annotation-based detector entirely, register a bean with its exact qualifier name to claim the `@ConditionalOnMissingBean(name = "...")` guard:

```java
// Disable the annotation-based detector — use only static JSON files
@Bean(name = "ordJavaAnnotationsDocumentSchemaDetector")
public DocumentSchemaDetector noOpAnnotationDetector() {
    return properties -> Map.of();
}
```

---

## Decision guide

| What you want | Recommended approach |
|---|---|
| Customise how one ORD entity type is generated from annotations | `EntityGeneratorFactoryCustomizer` + `withSupplier(...)` |
| Customise how one annotation type assembles its document section | `AnnotationProcessorFactoryCustomizer` + `withSupplier(...)` |
| Add mTLS support | `TLSAuthenticator` `@Bean` override |
| Live-reload documents without restart | `DocumentSchemaRegistry` `@Bean` override |
| Custom scanning strategy | `OrdAnnotationsScanner` `@Bean` override |
| Provide documents from a database or external API | Custom `DocumentSchemaDetector` — see `references/custom-detector.md` |
| Replace the entire factory pipeline | `EntityGeneratorFactory` / `AnnotationProcessorFactory` `@Bean` override |