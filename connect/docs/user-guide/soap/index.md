# Using the SOAP HTTP Connector

This connector allows you to easily connect to a SOAP service.


## Adding the maven dependency

To use the camunda connect SOAP connector in your project import the
`camunda-connect-bom` dependency with the current `VERSION`. Also add a
dependency either for the `camunda-connect-core` and
`camunda-connect-soap-http-client` or the `camunda-connect-connectors-all`
artifact. The later includes every artifact of the camunda connect project. If
you also use other components of the camunda BPM platform please use the
`camunda-bom` instead which already imports the `camunda-connect-bom`.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.camunda.connect</groupId>
      <artifactId>camunda-connect-bom</artifactId>
      <scope>import</scope>
      <type>pom</type>
      <version>${version.connect}</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

```xml
<dependencies>
  <dependency>
    <groupId>org.camunda.connect</groupId>
    <artifactId>camunda-connect-core</artifactId>
  </dependency>

  <dependency>
    <groupId>org.camunda.connect</groupId>
    <artifactId>camunda-connect-soap-http-client</artifactId>
  </dependency>
</dependencies>
```


## Getting a new SOAP HTTP connector

There a exists a `Connectors` class which automatically detects every connector in the
classpath. It can be used to generate a new connector instance by connector ID.

```java
SoapHttpConnector soap = Connectors.getConnector(SoapHttpConnector.ID);
```


## Creating a request

The SOAP HTTP connector can be used to create a new request, set a URL, content type
and payload.

```java
SoapHttpResponse response = connector.createRequest()
  .url("http://camunda.org/soap")
  .soapAction("doIt")
  .contentType("application/soap+xml")
  .payload(soap_envelope)
  .execute();
```

## Adding HTTP headers to a request

To add own headers to the HTTP request the method `header` is
available.

```java
SoapHttpResponse response = connector.createRequest()
  .url("http://camunda.org/soap")
  .soapAction("doIt")
  .contentType("application/soap+xml")
  .header("Accept", "application/xml")
  .payload(soap_envelope)
  .execute();
```


## Handle responses

A response contains the status code, response headers and body.

```java
Integer statusCode = response.getStatusCode();
String contentTypeHeader = response.getHeader("Content-Type");
String body = response.getResponse();
```

After the response was processed it should be closed.

```java
response.close()
```


## Add request interceptors


During the request invocation an interceptor chain is passed. The user can add own
interceptors to this chain. The interceptor is called for every request
of this connector.

```java
connector.addRequestInterceptor(interceptor).createRequest();
```


## Enable logging

camunda connect uses [camunda-commons-logging][] which itself uses [SLF4J][] as a logging backend.
To enable logging a SLF4J implementation has to be part of your classpath. For example
`slf4j-simple`, `log4j12` or `logback-classic`.

To also enable logging for the Apache HTTP client you can use a [SLF4J bridge][] like
`jcl-over-slf4j` as the Apache HTTP Client doesn't support SLF4J.


## Configure Apache HTTP client

camunda connect HTTP client uses the Apache HTTP client with its default configuration. If
you want to configure another connection manager or similar the easiest way is to register
a new connector factory.

```java
package org.camunda.connect.example;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.connect.httpclient.impl.AbstractHttpConnector;
import org.camunda.connect.spi.ConnectorConfigurator;

public class HttpConnectorConfigurator implements ConnectorConfigurator<HttpConnector> {

  public Class<HttpConnector> getConnectorClass() {
    return HttpConnector.class;
  }

  public void configure(HttpConnector connector) {
    CloseableHttpClient client = HttpClients.custom()
      .setMaxConnPerRoute(10)
      .setMaxConnTotal(200)
      .build();
    ((AbstractHttpConnector) connector).setHttpClient(client);
  }

}
```

To enable auto detection of your new configurator please add a file called
`org.camunda.bpm.connect.spi.ConnectorConfigurator` to your
`resources/META-INF/services` directory with class name as content.

```
org.camunda.connect.example.HttpConnectorConfigurator
```



[camunda-commons-logging]: https://github.com/camunda/camunda-commons/tree/master/logging
[SLF4J]: http://slf4j.org
[SLF4J bridge]: http://www.slf4j.org/legacy.html
