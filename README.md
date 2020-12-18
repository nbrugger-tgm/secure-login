# login-secure

[![GitHub issues by-label](https://img.shields.io/github/issues/nbrugger-tgm/secure-login/bug)](https://github.com/nbrugger-tgm/secure-login/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
[![GitHub closed issues by-label](https://img.shields.io/github/issues-closed/nbrugger-tgm/secure-login/bug)](https://github.com/nbrugger-tgm/secure-login/issues?q=is%3Aclosed+is%3Aissue+label%3Abug)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/nbrugger-tgm/secure-login?label=latest%20stable)](https://github.com/nbrugger-tgm/secure-login/releases/latest)
![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/nbrugger-tgm/secure-login/latest)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/nbrugger-tgm/secure-login?include_prereleases&label=latest)](github.com/nbrugger-tgm/secure-login/releases)

![GitHub last commit](https://img.shields.io/github/last-commit/nbrugger-tgm/secure-login)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/nbrugger-tgm/secure-login/Java%20JUnit%20Test%20with%20Gradle)
<br>

This lib provides ....

### Development

![Lines of code](https://img.shields.io/tokei/lines/github/nbrugger-tgm/secure-login)
<br>

[![Code Climate issues](https://img.shields.io/codeclimate/issues/nbrugger-tgm/secure-login?label=Code%20Quality%20issues)](https://codeclimate.com/github/nbrugger-tgm/secure-login)
[![Maintainability](https://img.shields.io/codeclimate/maintainability/nbrugger-tgm/secure-login.svg)](https://codeclimate.com/github/nbrugger-tgm/secure-login)
[![Maintainability](https://img.shields.io/codeclimate/maintainability-percentage/nbrugger-tgm/secure-login.svg)](https://codeclimate.com/github/nbrugger-tgm/secure-login)

### Usage

![Java 1.8](https://img.shields.io/badge/java-1.8-blue)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fniton.jfrog.io%2Fartifactory%2Fjava-libs%2Fcom%2Fniton%2Fsecure-login%2Fmaven-metadata.xml)](https://niton.jfrog.io/ui/repos/tree/General/java-libs%2Fcom%2Fniton%2Flogin-secure)

#### Gradle

```groovy
repositories {
    maven {
        url("https://niton.jfrog.io/artifactory/java-libs/")
    }
}
```

This is an API that streamlines login in a very secure system.
This system includes reporting of accounts which are in danger due to an attack.

## Flow
The process of the login handling is visualized here as it is kind of complex

![](login-security.png)

> The lower part of the image visualizes "listeners"

Here is the matching legend

![](login-security-legend.png)