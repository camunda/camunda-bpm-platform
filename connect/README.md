camunda-connect
===============

<p>
  <a href="http://camunda.org/">Home</a> |
  <a href="http://docs.camunda.org/latest/api-references/connect/">Documentation</a> |
  <a href="http://camunda.org/community/forum.html">Forum</a> |
  <a href="https://app.camunda.com/jira/browse/CAM">Issues</a> |
  <a href="LICENSE">License</a> |
  <a href="CONTRIBUTING.md">Contribute</a>
</p>

Simple API for connecting HTTP Services and other things.

# List of connectors

* HTTP Connector
* SOAP HTTP Connector

# Using a Connector

camunda Connect API aims at two usage scenarios, usage in a generic system such as camunda BPM
process engine and standalone usage via API.

## Standalone: Programmer friendly fluent API
For standalone usage in Java or Scripting Languages, the connectors expose a fluent API:

```java
// use connectors provider
SoapHttpConnector soapConnector = Connectors.getConnector(SoapHttpConnector.ID);
// or instantiate a new connector
soapConnector = new SoapHttpConnector();

SoapHttpResponse response = soapConnector.createRequest()
  .url("https://examaple.com/api/soap/21.0/")
  .soapAction("Login")
  .payload(requestMessage)
  .execute();
```

## Embedded: Generic API
For usage in a generic system such as camunda BPM process engine, the connectors expose a generic,
property-based API:

```java
SoapHttpConnector soapConnector = new SoapHttpConnector();

// create the request
SoapHttpRequest request = soapConnector.createRequest();

// configure the request
request.setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_URL, "https://examaple.com/api/soap/21.0/");
HashMap<String, String> headers = new HashMap<String, String>();
headers.put(SoapHttpRequest.HEADER_SOAP_ACTION, "Login");
request.setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_HEADERS, headers);
request.setRequestParameter(HttpBaseRequest.PARAM_NAME_REQUEST_PAYLOAD, requestMessage);

// execute the request
SoapHttpResponse response = request.execute();
```

# Contributing

camunda Connect is licensed under the Apache 2.0 License. Check [CONTRIBUTING.md][]
for guidelines about how to contribute.



[CONTRIBUTING.md]: https://github.com/camunda/camunda-bpm-platform/blob/master/CONTRIBUTING.md
