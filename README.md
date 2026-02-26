# mango4j

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=bitstep-ie_mango4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=bitstep-ie_mango4j)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=bitstep-ie_mango4j&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=bitstep-ie_mango4j)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=bitstep-ie_mango4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=bitstep-ie_mango4j)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=bitstep-ie_mango4j&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=bitstep-ie_mango4j)


[![CI](https://github.com/bitstep-ie/mango4j/actions/workflows/ci.yml/badge.svg)](https://github.com/bitstep-ie/mango4j/actions/workflows/ci.yml)
[![CodeQL](https://github.com/bitstep-ie/mango4j/actions/workflows/codeql.yml/badge.svg)](https://github.com/bitstep-ie/mango4j/actions/workflows/codeql.yml)
[![Dependabot](https://github.com/bitstep-ie/mango4j/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/bitstep-ie/mango4j/actions/workflows/dependabot/dependabot-updates)


<br />
<div align="center">
    <a href="https://github.com/bitstep-ie/mango4j">
    <picture>
        <source srcset="documentation/docs/assets/mango-with-text-black.png" media="(prefers-color-scheme: light)">
        <source srcset="documentation/docs/assets/mango-with-text-white.png" media="(prefers-color-scheme: dark)">
        <img src="documentation/docs/assets/mango-with-text-black.png" alt="mango Logo">
    </picture>
    </a>
    <h3 align="center">mango4j</h3>
    <p align="center">
        A collection of utility packages for java
        <br />
        <a href="https://bitstep-ie.github.io/mango4j/latest/"><strong>📚 Explore the docs »</strong></a>
        <br />
        <br />
        <a href="https://bitstep-ie.github.io/mango4j/latest/packages/collections/">🔎 View Examples</a>
        &middot;
        <a href="https://github.com/bitstep-ie/mango4j/issues/new?template=bug_report.md">
            🐛 Report Bug
        </a>
        &middot;
        <a href="https://github.com/bitstep-ie/mango4j/issues/new?template=feature_request.md">
            💡 Request Feature
        </a>
    </p>
</div>
<br />

Utility-first Java helper library providing a set of reusable modules that make everyday Java development cleaner, safer, and more ergonomic.

> This repository aggregates core reusable modules (collections, reflection helpers, validation utilities, etc.) under a single Maven project.


## 📦 What It Is
mango4j is a lightweight toolkit of utility modules for Java, designed to reduce boilerplate and improve code clarity. It includes:

- 📁 Collections utilities — helpers for richer data structure work
- 🔍 Reflection helpers — safer & simpler reflection abstractions
- 🧪 Validation utilities — common validation helpers
- 🔧 General utils — file, string, and other core helpers
- 🧠 Hibernate proxy resolver support (for ORM/EntityProxy handling)
Each module is standalone — use only what you need


## 🚀 Key Benefits

- Modular — pick the utilities you need without dragging unrelated code
- Clean API surface — focused helpers, no over-engineering
- Continuously tested via CI


## 📦 Installation


Maven:
```
<dependency>
  <groupId>ie.bitstep</groupId>
  <artifactId>mango4j</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:
```
implementation "ie.bitstep:mango4j:0.1.0"
```

> 📌 Adjust version if you’re using a newer release.


## 🧪 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) and adhere to the project’s code style and testing guidelines.


## 📘 Documentation

Auto-generated docs and examples live in the [project site](https://bitstep-ie.github.io/mango4j/latest/).

