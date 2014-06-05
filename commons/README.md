# camunda commons

camunda commons is a collection of shared libraries used by camunda open source projects.

## List of libraries

* [camunda commons logging][logging]
* [camunda commons utils][utils]

## Getting started

If your project is a maven project, start by importing the `camunda-commons-bom`.
This will ensure that your project uses all commons libraries in the same version:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.camunda.commons</groupId>
      <artifactId>camunda-commons-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Now you can reference individual commons projects:

```xml
<dependency>
  <groupId>org.camunda.commons</groupId>
  <artifactId>camunda-commons-logging</artifactId>
</dependency>
```

## FAQ

### Which Java (JRE) Version is required?

Java JRE 1.6+ is required.

### Under which License is this project distributed?

Apache License 2.0.

[logging]: logging/
[utils]: utils/

