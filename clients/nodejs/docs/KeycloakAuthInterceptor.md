# KeycloakAuthInterceptor

A KeycloakAuthInterceptor instance is an interceptor that adds a Bearer token header, containing 
a [Keycloak](https://www.keycloak.org/) access token, to all requests. This interceptor can be used
if the Camunda REST API is protected with [Keycloak Gatekeeper](https://github.com/keycloak/keycloak-gatekeeper).

This client serves also as an example for OpenID Connect based authentication. It shows also how an interceptor
with async functionality can be implemented with [hooks](https://github.com/sindresorhus/got#hooks), which are 
provided by the underlying [got](https://github.com/sindresorhus/got) HTTP request library. 

```js
const {
  Client,
  KeycloakAuthInterceptor
} = require("camunda-external-task-client-js");

const keycloakAuthentication = new KeycloakAuthInterceptor({
  tokenEndpoint: "https://your.keyclock.domain/realms/your-realm/protocol/openid-connect/token",
  clientId: "your-client-id",
  clientSecret: "your-client-secret"
});

const client = new Client({
  baseUrl: "http://localhost:8080/engine-rest",
  interceptors: keycloakAuthentication
});
```

## Caching

The Keycloak access token has an expiry defined. To reduce the requests to the token endpoint, we cache the token 
response as long it is valid.

To poll the API always with a valid token, we subtract the `cacheOffset` from the validity. If the token is 60 seconds
valid and we poll the API every 5 seconds, there could be the case that we poll the API exactly at the time the token 
expires. The default `cacheOffset` from 10 seconds is a good balance between the token validity and the average request 
duration.

## new KeycloakAuthInterceptor(options)

Here's a list of the available options:

| Option        | Description                           | Type   | Required | Default |
| ------------- | ------------------------------------- | ------ | -------- | ------- |
| tokenEndpoint | URL to the Keycloak token endpoint    | string | ✓        |         |
| clientId      | The Keycloak client id                | string | ✓        |         |
| clientSecret  | The Keycloak client secret            | string | ✓        |         |
| cacheOffset   | The time in seconds to subtract from the token expiry | number |          | 10      |
