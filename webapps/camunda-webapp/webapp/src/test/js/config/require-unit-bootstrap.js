// /**
//  * test bootstrap script
//  */

// (function(document, window, require) {

//   var pluginPackages = window.PLUGIN_PACKAGES || [];

//   require({
//     baseUrl: '/base/',
//     paths: {
//       'ngDefine' : 'main/webapp/assets/vendor/requirejs-angular-define/ngDefine',
//       'jquery' : 'main/webapp/assets/vendor/jquery/jquery',
//       'angular' : 'main/webapp/assets/vendor/angular/angular',
//       'angular-resource' : 'main/webapp/assets/vendor/angular/angular-resource',
//       'angular-sanitize' : 'main/webapp/assets/vendor/angular/angular-sanitize',
//       'angular-mocks': 'test/js/lib/angular/angular-mocks',
//       'bootstrap-slider': 'main/webapp/assets/vendor/bootstrap-slider/bootstrap-slider',
//       'jquery-overscroll' : 'main/webapp/assets/vendor/jquery/jquery.overscroll',
//       'jquery-mousewheel' : 'main/webapp/assets/vendor/jquery/jquery.mousewheel'
//     },
//     shim: {
//       'angular' : { deps: [ 'jquery' ], exports: 'angular' },
//       'angular-resource': { deps: [ 'angular' ] },
//       'angular-sanitize': { deps: [ 'angular' ] },
//       'angular-mocks': { deps: [ 'angular' ] },
//       'bootstrap-slider' : { deps: [ 'jquery' ] },
//       'jquery-overscroll': { deps: [ 'jquery' ] },
//       'jquery-mousewheel': { deps: [ 'jquery' ] },
//     },
//     packages: [
//       { name: 'cockpit', location: 'main/webapp/app/cockpit', main: 'cockpit' },
//       { name: 'cockpit-plugin', location: 'main/webapp/app/plugin' },
//       { name: 'base-plugin', location: 'main/webapp/plugin/base/app', main: 'plugin' },
//       { name: 'camunda-common', location: 'main/webapp/assets/vendor/camunda-common' },
//       { name: 'bpmn', location : 'main/webapp/assets/vendor/cabpmn' },
//       { name: 'dojo', location : 'main/webapp/assets/vendor/dojo/dojo' },
//       { name: 'dojox', location : 'main/webapp/assets/vendor/dojo/dojox' }
//     ].concat(pluginPackages)
//   });

//   var tests = [];
//   for (var file in window.__karma__.files) {
//     if (/Spec\.js$/.test(file)) {
//       tests.push(file);
//     }
//   }

//   require([
//     'angular',
//     'jquery',
//     'angular-resource',
//     'angular-sanitize',
//     'angular-mocks',
//     'ngDefine' ], function(angular, $) {

/* global require: false, console: false */
(function(document, window, require) {
  'use strict';

  // var pluginPackages = window.PLUGIN_PACKAGES || [];
  var projectTestExp = /^\/base\/src\/test\/js.*Spec\.js$/;

  require([
    '/base/target/webapp/require-conf.js'
  ], function(conf) {
    // test specific paths and shims
    conf.paths['angular-mocks'] = 'assets/vendor/angular-mocks/index';
    conf.shim['angular-mocks'] = ['angular'];

    conf.paths['unit-test'] = '/base/src/test/js/unit';

    require.config({
      baseUrl:  '/base/target/webapp',
      paths:    conf.paths,
      shim:     conf.shim,
      packages: conf.packages
    });

    var tests = [];
    for (var file in window.__karma__.files) {
      if (projectTestExp.test(file)) {
        tests.push(file);
      }
    }

    require([
      'angular',
      'jquery',
      'angular-resource',
      'angular-sanitize',
      'angular-mocks',
      'ngDefine'
    ], function(angular, $) {
      window._jQuery = $;
      window._jqLiteMode = false;

      tests.unshift('unit-test/browserTrigger');
      tests.unshift('unit-test/testabilityPatch');

      require(tests, function() {
        window.__karma__.start();
      });
    }, function(err) {
      console.info('The configuration is loaded... but still, it seems rotten.', err);
      throw err;
    });
  }, function(err) {
    console.info('Dude... The whole testing environment is screwed...', err.stack);
    throw err;
  });

})(document, window || this, require);
