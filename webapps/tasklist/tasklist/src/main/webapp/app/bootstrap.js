/**
 * bootstrap script of the tasklist application
 */

(function(document, require) {

  require.config({
    paths: {
      domReady : "assets/js/lib/require/domReady",
      angular : "assets/js/lib/angular/angular",
      resource : "assets/js/lib/angular/angular-resource",
      jquery: "assets/js/lib/jquery-1.7.2.min",
      angularReady : "common/util/angularReady",
      angularModule : "common/util/angularModule"
    },
    shim: {
      "angular" : { "exports" : "angular" }
    },
    priority: [
      "jquery",
      "angular"
    ],
    baseUrl: "../",
    packages: [
      { name: "tasklist", location: "app" },
      { name: "common", location: "common" }
    ]
  });

  require([ "angular", "jquery", "domReady!" ], function(angular, jquery) {
    if (!angular) {
      throw new Error("[dep] angular not loaded");
    }

    if (!jquery) {
      throw new Error("[dep] jquery not loaded");
    }
  });
  
  require([ "angular", "tasklist", "angularReady!" ], function(angular) {
    angular.bootstrap(document, ['tasklist']);
  });
})(document, require);