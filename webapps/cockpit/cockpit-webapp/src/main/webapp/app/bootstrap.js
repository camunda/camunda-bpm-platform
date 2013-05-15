/**
 * bootstrap script of the cockpit application
 */

(function(document, require) {

  var pluginPackages = PLUGIN_PACKAGES || [];

  require({
    baseUrl: '../',
    paths: {
      'ngDefine' : 'assets/vendor/requirejs-angular-define/ngDefine',
      'domReady' : 'assets/vendor/require/domReady',
      'jquery' : 'assets/vendor/jquery-1.7.2.min',
      'jquery-mousewheel' : 'assets/vendor/jquery.mousewheel',
      'jquery-overscroll' : 'assets/vendor/jquery.overscroll',
      'bootstrap' : 'assets/vendor/bootstrap/js/bootstrap',
      'bootstrap-slider' : 'assets/vendor/bootstrap-slider/bootstrap-slider',
      'angular' : 'assets/vendor/angular/angular',
      'angular-resource' : 'assets/vendor/angular/angular-resource'
    },
    shim: {
      'jquery-mousewheel' : { deps: [ 'jquery' ] },
      'jquery-overscroll' : { deps: [ 'jquery' ] },
      'bootstrap' : { deps: [ 'jquery' ] },
      'bootstrap-slider' : { deps: [ 'jquery' ] },
      'angular' : { deps: [ 'jquery' ], exports: 'angular' },
      'angular-resource': { deps: [ 'angular' ] }
    },
    packages: [
      { name: 'cockpit', location: 'app', main: 'cockpit' },
      { name: 'cockpit-plugin', location: 'app/plugin' },
      { name: 'camunda-common', location: 'assets/vendor/camunda-common' },
      { name: 'bpmn', location : 'assets/vendor/cabpmn' },
      { name: 'dojo', location : 'assets/vendor/dojo/dojo' },
      { name: 'dojox', location : 'assets/vendor/dojo/dojox' }
    ].concat(pluginPackages)
  });

  require([ 'angular', 'angular-resource', 'ngDefine', 'bootstrap' ], function(angular) {
    require([ 'cockpit', 'domReady!' ], function() {
      angular.bootstrap(document, ['cockpit']);
    });
  });

})(document, require);