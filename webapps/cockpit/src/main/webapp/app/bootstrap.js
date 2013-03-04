/**
 * bootstrap script of the cockpit application
 */

(function(document, require) {

  require.config({
    paths: {
      domReady : "assets/js/lib/require/domReady",
      jquery: "assets/js/lib/jquery-1.7.2.min",
      jqueryMousewheel : "assets/js/lib/jquery.mousewheel",
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
      { name: "dojox", location : "assets/js/lib/dojo/dojox" },
      { name: "jquery.overscroll", location : "assets/js/lib" },
      { name: "bootstrap-slider", location : "assets/bootstrap-slider" }
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