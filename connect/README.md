camunda-connect
===============

<p>
  <a href="http://camunda.org/">Home</a> |
  <a href="docs/index.md">Documentation</a> |
  <a href="http://camunda.org/community/forum.html">Forum</a> |
  <a href="https://app.camunda.com/jira/browse/CAM">Issues</a> |
  <a href="LICENSE">License</a> |
  <a href="CONTRIBUTING.md">Contribute</a>
</p>

Simple API for connecting HTTP Services and other things.

# List of connectors

* SOAP Http Connector

# Using a Connector

camunda Connect API aims at two usage scenarios, usage in a generic system such as camunda BPM 
process engine and standalone usage via API.

## Standalone: Programmer friendly fluent API
For standalone usage in Java or Scripting Languages, the connectors expose a fluent API:

```java
SoapHttpConnector soapConnector = new SoapHttpConnector();

SoapHttpResponse response = soapConnector.createRequest()
  .endpointUrl("https://examaple.com/api/soap/21.0/")
  .soapAction("Login")
  .soapEnvelope(requestMessage)
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
request.setRequestParameter(PARAM_NAME_ENDPOINT_URL, "https://examaple.com/api/soap/21.0/");
HashMap<String, String> headers = new HashMap<String, String>();
headers.put("SOAPAction", "Login");
request.setRequestParameter(PARAM_NAME_HEADERS, headers);
request.setRequestParameter(PARAM_NAME_SOAP_ENVELOPE, requestMessage);

// execure the request
SoapHttpResponse response = request.execute();
```

# Documentation

The sources of the documentation are located in the [docs folder](docs/index.md) of this repository.

# Contributing

camunda Connect is licensed under the Apache 2.0 License. Check [CONTRIBUTING.md](CONTRIBUTING.md) 
for guidelines about how to contribute.
