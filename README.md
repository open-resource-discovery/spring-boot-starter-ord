[![REUSE status](https://api.reuse.software/badge/github.com/open-resource-discovery/spring-boot-starter-ord)](https://api.reuse.software/info/github.com/open-resource-discovery/spring-boot-starter-ord)
[![CI](https://img.shields.io/github/actions/workflow/status/open-resource-discovery/spring-boot-starter-ord/ci.yml?label=CI)](https://github.com/open-resource-discovery/spring-boot-starter-ord/actions/workflows/ci.yml)
[![Maven version: spring-boot-starter-ord](https://img.shields.io/maven-central/v/org.open-resource-discovery/spring-boot-starter-ord?label=spring-boot-starter-ord)](https://central.sonatype.com/artifact/org.open-resource-discovery/spring-boot-starter-ord)

# Spring Boot Starter for ORD

Spring Boot auto-configuration for [Open Resource Discovery (ORD)](https://open-resource-discovery.org). Adds two endpoints to your application:

| Endpoint | Auth                         | Description |
|---|------------------------------|---|
| `GET /.well-known/open-resource-discovery` | None                         | Lists available ORD documents |
| `GET /ord/v1/documents/{document_name}` | mTLS, Basic Auth (or `open`) | Returns an ORD document |

## AI Coding Agent Support

A set of dedicated AI agent skills can be found under [.skills/](.skills/). These can be used to enable AI agents to interact with this project in various ways, such as generating documentation, answering questions about the codebase, or assisting with development tasks.

## Requirements

- Java 17+
- Spring Boot 3.x (tested against 3.5.x)

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

The starter scans the listed packages, generates an ORD document, and serves it at `/ord/v1/documents/<document-name>` (see: `@Ord.Document` annotation).

### Option B — Static files

Place a pre-built ORD document JSON file on the classpath and declare it:

```yaml
ord:
  documents:
    - name: my-service
      path: classpath:ord/my-document.json
      accessStrategies:
        - open   # omit to require Basic Auth
```

The starter serves the ORD document at `/ord/v1/documents/my-service`.

### Authentication

Document endpoints require HTTP Basic Auth by default. Credentials can be defined in `application.yml`:

```yaml
ord:
  credentials:
    my-user: {bcrypt}<password-hash>
```

To make a specific document publicly accessible without credentials, set `accessStrategies: [open]` on that document (see Option B above). The `open` access can be combined with others (visibility filtering applies as usual).

> [!IMPORTANT]
> The `/.well-known/open-resource-discovery` endpoint is always unauthenticated.

#### Open

When the `open` authentication parameter is used, the server bypasses authentication checks.

#### Basic Authentication

The server supports Basic Authentication through application properties mapping usernames to bcrypt-hashed passwords:

```yaml
ord:
  credentials:
    my-user: {bcrypt}<password-hash>
```

To generate hashes, use [htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html) utility:

```bash
htpasswd -Bnb <user> <password>
```

This will output something like `admin:$2y$05$...` - replace `<password-hash>` with the actual hash part (starting with `$2y$`) in your configuration.

> [!IMPORTANT]
> Make sure to use strong passwords and handle the credentials securely. Never commit real credentials to version control.

<details>
<summary>Using htpasswd in your environment</summary>

- **Platform independent**:

  > Prerequisite is to have [NodeJS](https://nodejs.org/en) installed on the machine.

  ```bash
  npm install -g htpasswd
  ```

  After installing package globally, command `htpasswd` should be available in the Terminal.

- **macOS**:

  Installation of any additional packages is not required. Utility `htpasswd` is available in Terminal by default.

- **Linux**:

  Install apache2-utils package:

  ```bash
  # Debian/Ubuntu
  sudo apt-get install apache2-utils

  # RHEL/CentOS
  sudo yum install httpd-tools
  ```

</details>

#### mTLS Authentication

##### Cloud Foundry mTLS Authentication (see: [here](https://docs.cloudfoundry.org/adminguide/securing-traffic.html#gorouter_mutual_auth))

For SAP BTP CloudFoundry environments, mTLS (mutual TLS) authentication is supported using client certificate headers. Configure and register a bean of type [CloudFoundryTLSAuthenticator](src/main/java/org/openresourcediscovery/core/security/impl/CloudFoundryTLSAuthenticator.java) to enable.

##### Kyma mTLS Authentication (see: [here](https://kyma-project.io/external-content/api-gateway/docs/user/istio-gateways/mtls-context.html#the-mtls-authentication-flow))

For SAP BTP Kyma environments, mTLS (mutual TLS) authentication is supported using client certificate headers. Configure and register a bean of type [KymaTLSAuthenticator](src/main/java/org/openresourcediscovery/core/security/impl/KymaTLSAuthenticator.java) to enable.

##### Wildcard Support

You can use `"*"` as a wildcard for `issuer` and/or `subject` fields:

| Configuration                                     | Behavior                                               |
| ------------------------------------------------- | ------------------------------------------------------ |
| `issuer: "*"`                                     | Accept any issuer, validate subject only               |
| `subject: "*"`                                    | Accept any subject, validate issuer only               |
| Both `"*"`                                        | Skip issuer/subject validation, only validate rootCaDn |

> [!NOTE]
> For SAP BTP CloudFoundry environments `trustedRootCertificateAuthorityDN` validation is always enforced and cannot be bypassed with wildcards.

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

## Code of Conduct

We as members, contributors, and leaders pledge to make participation in our community a harassment-free experience for everyone. By participating in this project, you agree to abide by its [Code of Conduct](CODE_OF_CONDUCT.md) at all times.

## Licensing

Copyright 2026 SAP SE or an SAP affiliate company and <your-project> contributors. Please see our [LICENSE](LICENSE) for copyright and license information. Detailed information including third-party components and their licensing/copyright information is available [via the REUSE tool](https://api.reuse.software/info/github.com/open-resource-discovery/spring-boot-starter-ord).