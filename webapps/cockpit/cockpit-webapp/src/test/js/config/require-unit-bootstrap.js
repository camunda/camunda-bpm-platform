/**
 * test bootstrap script
 */

(function(document, window, require) {

  var pluginPackages = window.PLUGIN_PACKAGES || [];

  require({
    baseUrl: '/base/',
    paths: {
      'ngDefine' : 'main/webapp/assets/vendor/requirejs-angular-define/ngDefine',
      'jquery' : 'main/webapp/assets/vendor/jquery-1.7.2.min',
      'angular' : 'main/webapp/assets/vendor/angular/angular',
      'angular-resource' : 'main/webapp/assets/vendor/angular/angular-resource'
    },
    shim: {
      'angular' : { deps: [ 'jquery' ], exports: 'angular' },
      'angular-resource': { deps: [ 'angular' ] }
    },
    packages: [
      { name: 'cockpit', location: 'main/webapp/app', main: 'cockpit' },
      { name: 'cockpit-plugin', location: 'main/webapp/app/plugin' },
      { name: 'camunda-common', location: 'main/webapp/assets/vendor/camunda-common' },
      { name: 'bpmn', location : 'main/webapp/assets/vendor/cabpmn' },
      { name: 'dojo', location : 'main/webapp/assets/vendor/dojo/dojo' },
      { name: 'dojox', location : 'main/webapp/assets/vendor/dojo/dojox' }
    ].concat(pluginPackages)
  });

  var tests = [];
  for (var file in window.__karma__.files) {
    if (/Spec\.js$/.test(file)) {
      tests.push(file);
    }
  }

  require([ 'angular', 'angular-resource', 'ngDefine' ], function(angular) {
    require(tests, function() {
      window.__karma__.start();
    });
  });

})(document, window || this, require);