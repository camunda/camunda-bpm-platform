# BasicAuthInterceptor

A BasicAuthInterceptor instance is a simple interceptor that adds basic authentication to all requests.

```js
const {
  Client,
  BasicAuthInterceptor
} = require("camunda-external-task-client-js");

const basicAuthentication = new BasicAuthInterceptor({
  username: "demo",
  password: "demo"
});

const client = new Client({
  baseUrl: "http://localhost:8080/engine-rest",
  interceptors: basicAuthentication
});
```

## new BasicAuthInterceptor(options)

Here's a list of the available options:

| Option   | Description                           | Type   | Required | Default |
| -------- | ------------------------------------- | ------ | -------- | ------- |
| username | username used in basic authentication | string | ✓        |         |
| password | password used in basic authentication | string | ✓        |         |
