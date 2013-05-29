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
      'angular-resource' : 'main/webapp/assets/vendor/angular/angular-resource',
      'angular-mocks': 'test/js/lib/angular/angular-mocks'
    },
    shim: {
      'angular' : { deps: [ 'jquery' ], exports: 'angular' },
      'angular-resource': { deps: [ 'angular' ] },
      'angular-mocks': { deps: [ 'angular' ] }
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

  require([
    'angular',
    'jquery',
    'angular-resource',
    'angular-mocks',
    'ngDefine' ], function(angular, $) {

    window._jQuery = $;
    window._jqLiteMode = false;

    tests.unshift('/base/test/js/unit/testabilityPatch.js');
    
    require(tests, function() {
      window.__karma__.start();
    });
  });

})(document, window || this, require);