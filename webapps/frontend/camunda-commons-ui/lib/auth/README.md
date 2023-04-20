# commons-ui-auth

Authentication module for camunda webapps.


## Implementation

The authentication tools are relying on the camunda-commons-ui utilities, here's an example of how to load the modules with [requirejs](http://requirejs.org).

```js
require.config({
  // http://requirejs.org/docs/api.html#paths
  paths: {
    angular:                    'path/to/angular',
    'camunda-commons-ui':       'path/to/camunda-commons-ui/lib'
  },
  
  // http://requirejs.org/docs/api.html#shim
  shim: {
    angular: {
      exports: 'angular'
    },
    'camunda-commons-ui/util': ['angular'],
    'camunda-commons-ui/auth': ['camunda-commons-ui-util']
  },

  // http://requirejs.org/docs/api.html#packages
  packages: [
    {
      name: 'camunda-commons-ui/util'
      main: 'index'
    },
    {
      name: 'camunda-commons-ui/auth',
      main: 'index'
    }
  ]
});

require([ // or `define`
  'angular',
  'camunda-commons-ui-auth'
], function(
  angular,
  camAuth
) {
  // camAuth is a angularjs module and therefor has a "name" property
  // who can be used to solve dependencies
  var ngModule = angular.module('my-ng-module-name', [
    camAuth.name
  ]);
});
```


## Secure Routes

Secure your route by configuring the `authenticated` flag in a route definition.

```js
ngModule.config(function(routeProvider) {
  routeProvider.when('/restricted', {
    controller: 'some-controller',
    authentication: 'required'
  });
});
```

The `authenticated` flag may either be

* `required`: if the user is not authenticated, the application redirects to `/login`
* `optional`: the user may or may not be authenticated

Failing to authenticate for a route triggers the `authentication.login.required` event.


## Events

You may hook into the life-cycle via the following events that are broadcasted through the `$rootScope`

* `authentication.login.required`
* `authentication.login.success`
* `authentication.login.failure`
* `authentication.logout.success`
* `authentication.logout.failure`

Not suppressing an event via `event.preventDefault()` leads to the execution of its default action.


## Accessing user credentials

The currently logged in user may always be accessed via `$rootScope.authentication` or via `authentication` in the context of a route controller.


### In templates

```xml
<navigation>
  <ng-if="authentication">
    <a ng-click="logout()">logout</a>
  </ng-if>
</navigation>
```


### In routes

Configure a route with the `authentication` flag to access the currently authenticated user in a route controller.

```js
function MyRouteController($scope, authentication) {

  if (authentication == null) {
    // user is not logged in
  } else {
    authentication.canAccess('tasklist') ...
  }
}

ngModule.config(function(routeProvider) {

  routeProvider.when('/my-route', {
    controller: MyRouteController,
    authentication: 'required' || 'optional'
  });
});
```

### In any other place

Use `$rootScope.authentication` to access the current authentication any time.

```js
ngModule.service('myService', function($rootScope) {

  if ($rootScope.authentication) {
    // authenticated
  } else {
    // not authenticated
  }
});
```


## Components

### Authentication

Holds the user credentials, if they exist. Bound to `$rootScope.authentication`.


### AuthenticationService

May be used to login and logout users from an application and allows the application to query the currently logged in user from the backend.

#### api methods

*   `login(username:String, password:String): Promise<Authentication, Error>`

    Tries to login the user with the specified credentials. Returns a promise that either resolves
    to the logged in users `Authentication` or an `Error` if the authentication failed.

*   `logout(): Promise<None,Error>`

    Logs the user out of the application.

*   `getAuthentication(): Promise<Authentication, Error>`

    Returns the locally known logged in user or tries to fetch it from the backend (in case a server-side session exists).


#### events

The service emits events. An events default action may be suppressed by calling `event.preventDefault()`.

*   `authentication.login.required` a login is required to access a secured route; default action: redirect to `/login`
*   `authentication.login.success`: login succeeded; default action: redirect to `/`
*   `authentication.login.failure`: login failed

*   `authentication.logout.success`: logout succeeded; default action: redirect to `/`
*   `authentication.logout.failure`: logout failed


### Directives

Use the directives `cam-if-logged-in` and `cam-if-logged-out` to conditionally show elements based on whether the user is logged in.

```xml
<h1>Overview</h1>

<div cam-if-logged-in>
  <div user-dashboard></div>
</div>

<div cam-if-logged-out>
  <a>Login to access your dashboard</a>
</div>
```


## Features

### Interception of 401 (unauthorized)

The camunda-commons-ui module adds interception of 401 responses (unauthorized) to ensure that a server side session timeout is correctly handled.

The behavior is defined in `lib/pages/index`. It defaults to emitting a `authentication.login.required` event and resetting the client side authentication.


### Redirect after login

The camunda-commons-ui/auth module adds automatic redirect after login functionality.

The behavior is defined in `lib/auth/index`. It defaults to capturing the current request url on `authentication.login.required` and redirecting to that url on `authentication.login.success`. The behavior may be disabled by silencing the respective events.


## Examples

A login controller:

```js
function MyController($scope, AuthenticationService) {

  $scope.login = function() {
    AuthenticationService.login(username, password)
      .then(function(credentials) {
        // authenticated successfully
      }, function(error) {
        // authentication failed
      });
  };
}
```

