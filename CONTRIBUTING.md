# How to Contribute

* [Ways to Contribute](#ways-to-contribute)
* [Build from Source](#build-from-source)
* [Create a Pull Request](#create-a-pull-request)
* [Contribution Checklist](#contribution-checklist)
* [Contributor License Agreement (CLA)](#contributor-license-agreement-cla)
* [Commit Message Conventions](#commit-message-conventions)
* [License Headers](#license-headers)
* [Review Process](#review-process)

# Ways to Contribute

We would love you to contribute to this project. You can do so in various ways.


## Contribute your Knowledge

Help others by participating in our [forum](https://forum.camunda.org/). Please read the [Forum FAQ](https://forum.camunda.org/faq) before you start.


## File Bugs or Feature Requests

File bugs you found in the code or features you would like to see in the future in our [JIRA project named CAM](https://jira.camunda.com/browse/CAM).

1. [Signup for our JIRA](https://jira.camunda.com/secure/Signup!default.jspa)
1. [Search for similar issues](https://jira.camunda.com/issues/?jql=project%20%3D%20CAM), your bug or feature may have already been reported
1. [Create a new ticket](https://jira.camunda.com/secure/CreateIssue!default.jspa)
   1. Select the CAM project
   1. Be concise
   1. Describe the steps to reproduce the problem or the context of the feature request
   1. Specify your environment (e.g. Camunda version, Camunda modules you use, ...)
   1. Provide code. For a bug report, create a test that reproduces the problem. For feature requests, create mockup code that shows how the feature might look like. Fork our [unit test Github template](https://github.com/camunda/camunda-engine-unittest) to get started quickly.


## Write Code

You can contribute code that fixes bugs and/or implements features. Here is how it works:

1. Select a ticket that you would like to implement. Tickets labelled [EasyPick](https://jira.camunda.com/issues/?jql=project%20%3D%20CAM%20AND%20status%20in%20(Open%2C%20Reopened%2C%20Ready)%20AND%20labels%20in%20(easy-pick%2C%20EasyPick)) are tickets that require little effort and can be good candidates to start. Be aware though that some of them need good knowledge of the surrounding code.
1. Tell us in the ticket comments or in the [forum](https://forum.camunda.org/c/contributions/14) (select the *Contributions* category) that you want to work on your ticket. This is also the place where you can ask questions.
1. Check your code changes against our [contribution checklist](#contribution-checklist)
1. [Create a pull request on Github](). Note that you can already do this earlier if you would like feedback on your work in progress.


# Build from Source

In order to build our codebase from source, add the following to your Maven `settings.xml`.

```xml
<profiles>
  <profile>
    <id>camunda-bpm</id>
    <repositories>
      <repository>
        <id>camunda-bpm-nexus</id>
        <name>camunda-bpm-nexus</name>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
        <url>https://app.camunda.com/nexus/content/groups/public</url>
      </repository>
    </repositories>
  </profile>
</profiles>
<activeProfiles>
  <activeProfile>camunda-bpm</activeProfile>
</activeProfiles>
```

An entire repository can then be built by running `mvn clean install` in the root directory. This will build all sub modules and execute unit tests. Furthermore, you can restrict the build to just the module you are changing by running the same command in the corresponding directory. Check the repository's or module's README for additional module-specific instructions. The `webapps` and `swagger-ui` modules requires NodeJS. You can exclude building them by running `mvn clean install -pl '!webapps,!org.camunda.bpm.run:camunda-bpm-run-modules-swaggerui'`.

Integration tests (e.g. tests that run in an actual application server) are usually not part of the default Maven profiles. If you think they are relevant to your contribution, please ask us in the ticket, on the forum or in your pull request for how to run them. Smaller contributions usually do not need this.

# Create a Pull Request

In order to show us your code, you can create a pull request on Github. Do this when your contribution is ready for review, or if you have started with your implementation and want some feedback before you continue. It is always easier to help if we can see your work in progress.

A pull request can be submitted as follows: 

1. [Fork the Camunda repository](https://docs.github.com/en/github/getting-started-with-github/fork-a-repo) you are contributing to
1. Commit and push your changes to a branch in your fork
1. [Submit a Pull Request to the Camunda repository](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request-from-a-fork). As the *base* branch (the one that you contribute to), select `master`. This should also be the default in the Github UI.

# Contribution Checklist

Before submitting your pull request for code review, please go through the following checklist:

1. Is your code formatted according to our code style guidelines?
    * Java: Please check our [Java Code Style Guidelines](https://github.com/camunda/camunda-bpm-platform/wiki/Coding-Style-Java). You can also import [our template and settings files](https://github.com/camunda/camunda-bpm-platform/tree/master/settings) into your IDE before you start coding.
    * Javascript: Your code is automatically formatted whenever you commit.
1. Is your code covered by unit tests?
    * Ask us if you are not sure where to write the tests or what kind of tests you should write.
    * Java: Please follow our [testing best practices](https://github.com/camunda/camunda-bpm-platform/wiki/Testing-Best-Practices-Java).
    * Have a look at other tests in the same module for how it works.
    * In rare cases, it is not feasible to write an automated test. Please ask us if you think that is the case for your contribution.
1. Do your commits follow our [commit message conventions](#commit-message-conventions)?
1. Does your code use the [correct license headers](#license-headers)?

# Contributor License Agreement (CLA)

Before we can merge your contribution you have to sign our [Contributor License Agreement](https://cla-assistant.io/camunda/) (CLA). The CLA contains the terms and conditions under which the contribution is submitted. You need to do this only once for your first pull request. Keep in mind that without a signed CLA we cannot merge your contribution.

# Commit Message Conventions

The messages of all commits must conform to the style:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Example:

```
feat(engine): Support BPEL

- implements execution for a really old standard
- BPEL models are mapped to internal ActivityBehavior classes

related to CAM-1337
```

Have a look at the [commit history](https://github.com/camunda/camunda-bpm-platform/commits/master) for real-life examples.


## \<type\>

One of the following:

* feat (feature)
* fix (bug fix)
* docs (documentation)
* style (formatting, missing semi colons, …)
* refactor
* test (when adding missing tests)
* chore (maintain)
 
## \<scope\>

The scope is the module that is changed by the commit. E.g. `engine` in the case of https://github.com/camunda/camunda-bpm-platform/tree/master/engine.

## \<subject\>

A brief summary of the change. Use imperative form (e.g. *implement* instead of *implemented*).  The entire subject line shall not exceed 70 characters.

## \<body\>

A list of bullet points giving a high-level overview of the contribution, e.g. which strategy was used for implementing the feature. Use present tense here (e.g. *implements* instead of *implemented*). A line in the body shall not exceed 80 characters. For small changes, the body can be omitted. 

## \<footer\>

Must be `related to <ticket>` where ticket is the ticket number, e.g. CAM-1234. If the change is related to multiple tickets, list them in a comma-separated list such as `related to CAM-1234, CAM-4321`.

# License Headers

Every source file in an open-source repository needs to contain the following license header at  the top, formatted as a code comment:

```
Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
under one or more contributor license agreements. See the NOTICE file
distributed with this work for additional information regarding copyright
ownership. Camunda licenses this file to you under the Apache License,
Version 2.0; you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

The header can be added manually (check other files). If you use our [IDE settings](https://github.com/camunda/camunda-bpm-platform/tree/master/settings), it will be generated automatically when you create new `.java` files. You can also add it by running `mvn clean install -Plicense-header-check` in the module that you have changed. This command also re-formats any incorrectly formatted license header.

Contributions that do not contain valid license headers cannot be merged.

# Review Process

We usually check for new community-submitted pull requests once a week. We will then assign a reviewer from our development team and that person will provide feedback as soon as possible. 

Note that due to other responsibilities (our own implementation tasks, releases), feedback can sometimes be a bit delayed. Especially for larger contributions, it can take a bit until we have the time to assess your code properly.

During review we will provide you with feedback and help to get your contribution merge-ready. However, before requesting a review, please go through our [contribution checklist](#contribution-checklist).

Once your code is merged, it will be shipped in the next alpha and minor releases. We usually build alpha releases once a month and minor releases once every six months. If you are curious about the exact next minor release date, check our [release announcements](https://docs.camunda.org/enterprise/announcement/) page.
