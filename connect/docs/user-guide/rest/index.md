# Using REST Connector

This connector allows you to easily connect to a REST service.

## Creating the request

You can use the `.createRequest()` method to create a plain request.

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
```

## Defining a request url

To define a request url you can use the `.requestUrl(String url)` method. 
This allows you to set an endpoint for your request. 

Let's use the previous example and extend it for this method:

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
  request.requestUrl("http://camunda.org");
```

## Defining additional headers

You can add any additional header that you want to your request. To do this, you can use the
`.header(String name, Object value)` method. This will append the new header to the request.

In our example we will add an Accept header for our request:

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
  request.requestUrl("http://camunda.org");
  request.header("Accept", "application/json");
```

Now the server knows that we want a JSON response to our rest call.

## Defining the payload

For POST, PUT and PATCH requests you have to attach a request body to change data
on the server side. For this, there is a `.requestPayload(String payload)` method.

So let's add some informations as payload to our example:

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
  request.requestUrl("http://camunda.org");
  request.header("Accept", "application/json");
  request.payload("{\"text\": \"camunda rocks!\"}");
```

## Defining the request type

As we know, an HTTP request must have at least one of the following request types:

* GET
* PUT
* POST
* PATCH
* DELETE

So there's be also a way to set this request type in our example. For this we have 
one of the following methods:

* `.get()`
* `.post()`
* `.put()`
* `.patch()`
* `.delete()`

With this we have a full request which we can use to connect our REST service:

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
  request.requestUrl("http://camunda.org");
  request.header("Accept", "application/json");
  request.payload("{\"text\": \"camunda rocks!\"}");
  request.post()
```

## Execute the request

Now that we have a complete request, we can trigger it with the `.execute()` method.
After the execution, we get a RestHttpResponse as answer to our request. This response
lets us fetch two main pieces of information of the response: the status code and the response 
body. The status code can be fetched with the `.getStatusCode()` method and the response 
body with the `.getResponse()` method.

So, to complete our example we'll add the execution and also some response handling:

```java
  RestHttpConnector connector = new RestHttpConnector();
  RestHttpRequest request = connector.createRequest();
  request.requestUrl("http://camunda.org");
  request.header("Accept", "application/json");
  request.payload("{\"text\": \"camunda rocks!\"}");
  request.post();
  
  RestHttpResponse response = request.execute();
  Integer statusCode = response.getStatusCode();
  String responseBody = response.getResponse();
```
