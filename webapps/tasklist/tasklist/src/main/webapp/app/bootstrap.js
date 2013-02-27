/**
 * bootstrap script of the tasklist application
 */

(function(document, require) {

  require.config({
    paths: {
      domReady : "assets/js/lib/require/domReady",
      jquery: "assets/js/lib/jquery-1.7.2.min",
      bootstrap: "assets/bootstrap/js/bootstrap",
      angularReady : "common/util/angularReady",
      angularModule : "common/util/angularModule"
    },
    shim: {
      "angular" : { "exports" : "angular" }
    },
    priority: [
      "jquery",
      "angular",
      "bootstrap"
    ],
    baseUrl: "../",
    packages: [
      { name: "tasklist", location: "app" },
      { name: "common", location: "common" },
      { name: "angular", location : "assets/js/lib/angular", main: "angular"}
    ]
  });

  require([ "jquery", "bootstrap", "angular", "tasklist", "angularReady!" ], function(bs, $, angular) {
    require(["angular/angular-resource"], function () {
      angular.bootstrap(document, ['tasklist']);
    });
  });

})(document, require);