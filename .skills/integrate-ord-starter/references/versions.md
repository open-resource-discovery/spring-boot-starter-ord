# Version pins

Every version-sensitive claim in this skill, in one place. When updating the skill, check these first.

| Claim                                                         | Where stated | Last verified |
|---------------------------------------------------------------|---|---|
| `spring-boot-starter-ord:1.0.0`                               | `SKILL.md` (dependency snippets) | 2026-06 |
| ORD schema `"openResourceDiscovery": "1.16"`                  | `SKILL.md` (static JSON example, custom-detector.md) | 2026-06 |
| ORD JSON Schema URL `spec-v1/interfaces/Document.schema.json` | `SKILL.md` (static JSON example) | 2026-06 |

## How to verify

```bash
# Latest starter version on Maven Central
curl -s "https://central.sonatype.com/api/v1/publisher/published?namespace=org.open-resource-discovery&name=spring-boot-starter-ord" \
  | jq -r '.version'

# Current ORD specification version
curl -sI https://open-resource-discovery.github.io/specification/spec-v1/interfaces/Document.schema.json | head -1
```
