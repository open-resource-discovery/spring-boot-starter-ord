# Contributing

## Code of Conduct

All members of the project community must abide by the [SAP Open Source Code of Conduct](https://github.com/open-resource-discovery/.github/blob/main/CODE_OF_CONDUCT.md).  
Only by respecting each other can we build a productive, collaborative community.  
Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting [a project maintainer](.reuse/dep5).

---

## Engaging in Our Project

We use GitHub to manage issues and review pull requests.

- If you are a new contributor, see: [Steps to Contribute](#steps-to-contribute)
- Before implementing your change, create an issue describing the problem or enhancement.
- Indicate that you would like to work on the issue.
- The team will review and either assign it to you or provide feedback.

---

## Steps to Contribute

- Claim an issue by commenting on it to avoid duplicate work.
- If you have questions, ask in the issue and a maintainer will help.

---

## Contributing Code or Documentation

You are welcome to contribute code to fix bugs, improve performance, or add new features.

Please follow these rules:

- Contributions must be licensed under the [Apache 2.0 License](./LICENSE).
- You must agree to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/) when submitting your first PR.
- Contributions must follow our [guidelines on AI-generated code](https://github.com/open-resource-discovery/.github/blob/main/CONTRIBUTING_USING_GENAI.md) if applicable.

---

## Issues and Planning

- Use GitHub Issues to report bugs or request enhancements.
- Provide clear reproduction steps, logs, and context to help us understand the issue.

---

## Java Coding Guidelines

We follow standard Java conventions:

- Use 4 spaces for indentation.
- Use `camelCase` for variables/methods and `PascalCase` for classes.
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) unless otherwise noted.

We recommend using:
- [Spotless](https://github.com/diffplug/spotless) for formatting
- [Checkstyle](https://checkstyle.org/) or [PMD](https://pmd.github.io/) for static analysis

Run these locally before pushing code.

---

## Development Setup

### Prerequisites

- Java 21
- Maven 3.9+

### Initial Setup

```bash
# Clone the repository
git clone <repository-url>
cd <project-dir>

# Build the project
./mvnw clean install

# Run tests
./mvnw test

