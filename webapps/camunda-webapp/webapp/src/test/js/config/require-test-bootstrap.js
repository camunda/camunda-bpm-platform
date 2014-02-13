/* global require: false, ngDefine: false, console: false */
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

      ngDefine.debug = true;

      window._jQuery = $;
      window._jqLiteMode = false;

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
