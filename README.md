# login-secure

[![GitHub issues by-label](https://img.shields.io/github/issues/nbrugger-tgm/JAuth/bug)](https://github.com/{{repo}}/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
[![GitHub closed issues by-label](https://img.shields.io/github/issues-closed/nbrugger-tgm/JAuth/bug)](https://github.com/{{repo}}/issues?q=is%3Aclosed+is%3Aissue+label%3Abug)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/nbrugger-tgm/JAuth?label=latest%20stable)](https://github.com/{{repo}}/releases/latest)
![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/nbrugger-tgm/JAuth/latest)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/nbrugger-tgm/JAuth?include_prereleases&label=latest)](github.com/{{repo}}/releases)

![GitHub last commit](https://img.shields.io/github/last-commit/nbrugger-tgm/JAuth)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/nbrugger-tgm/JAuth/Java%20JUnit%20Test%20with%20Gradle)
<br>

This lib provides ....

### Development

![Lines of code](https://img.shields.io/tokei/lines/github/nbrugger-tgm/JAuth)
<br>

[![Code Climate issues](https://img.shields.io/codeclimate/issues/nbrugger-tgm/JAuth?label=Code%20Quality%20issues)](https://codeclimate.com/github/{{repo}})
[![Maintainability](https://img.shields.io/codeclimate/maintainability/nbrugger-tgm/JAuth.svg)](https://codeclimate.com/github/{{repo}})
[![Maintainability](https://img.shields.io/codeclimate/maintainability-percentage/nbrugger-tgm/JAuth.svg)](https://codeclimate.com/github/{{repo}})

### Usage

![Java 1.8](https://img.shields.io/badge/java-1.8-blue)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fniton.jfrog.io%2Fartifactory%2Fjava-libs%2Fcom%2Fniton%2Fjauth%2Fmaven-metadata.xml)](https://niton.jfrog.io/ui/repos/tree/General/java-libs%2Fcom%2Fniton%2F{{name}})

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