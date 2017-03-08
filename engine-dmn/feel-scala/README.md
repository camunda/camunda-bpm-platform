# camunda-feel-integration

Provide an integration of the [FEEL Engine](https://github.com/saig0/feel) into camunda BPM using the SPI of the [camunda DMN engine](https://github.com/camunda/camunda-engine-dmn). 

## Goal

Using the FEEL engine to evaluate expressions of DMN decision tables that are evaluated by the camunda DMN engine.

## How to use

If you use Java to build the camunda DMN engine then you have to set the FEEL Engine Factory:

```java
DefaultDmnEngineConfiguration dmnEngineConfig = (DefaultDmnEngineConfiguration) DmnEngineConfiguration.createDefaultDmnEngineConfiguration(); 
dmnEngineConfig.setFeelEngineFactory(new CamundaFeelEngineFactory());
// more configs ...
DmnEngine engine = dmnEngineConfig.buildEngine();
```

## How to build

> Requirements
* [SBT](http://www.scala-sbt.org) to build and test the application

First, make sure that you have built the [FEEL Engine](https://github.com/saig0/feel#how-to-build). It is not available on public repository yet.

Run the tests with
```
sbt test
```

Build the jar including all dependencies with
```
sbt assemply
```
