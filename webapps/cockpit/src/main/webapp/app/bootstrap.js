/**
 * bootstrap script of the cockpit application
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
      { name: "cockpit", location: "app", main: "cockpit" },
      { name: "common", location: "common" },
      { name: "angular", location : "assets/js/lib/angular", main: "angular" },
      { name: "bpmn", location : "assets/js/lib/bpmn" },
      { name: "dojo", location : "assets/js/lib/dojo/dojo" },
      { name: "dojox", location : "assets/js/lib/dojo/dojox" }
    ]
  });

  require([ "jquery"], function($) {
    require(["bootstrap", "angular", "cockpit", "angularReady!" ], function(bs, angular) {
      require(["angular/angular-resource"], function () {
        angular.bootstrap(document, ['cockpit']);
      });
    });
  }); 

})(document, require);