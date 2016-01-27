// /**
//  *  Hack to support amd with angular-scenario
//  *
//  *  @see http://stackoverflow.com/questions/15499997/how-to-use-angular-scenario-with-requirejs
//  */
// (function() {
//   var setUpAndRun = angular.scenario.setUpAndRun;
//
//   angular.scenario.setUpAndRun = function(config) {
//     amdSupport();
// //        if (config.useamd) {
// //            amdSupport();
// //        }
//     return setUpAndRun.apply(this, arguments);
//   };
//
//   function amdSupport() {
//     var getFrame_ = angular.scenario.Application.prototype.getFrame_;
//
//     /**
//      *  This function should be added to angular-scenario to support amd. It overrides the load behavior to wait from
//      *  the inner amd frame to be ready.
//      */
//     angular.scenario.Application.prototype.getFrame_ = function() {
//       var frame = getFrame_.apply(this, arguments);
//       var load = frame.load;
//
//       frame.load = function(fn) {
//         if (typeof fn === 'function') {
//           angular.element(window).bind('message', function(e) {
//             if (e.data && e.source === frame.prop('contentWindow') && e.data.type === 'loadamd') {
//               fn.call(frame, e);
//             }
//           });
//           return this;
//         }
//         return load.apply(this, arguments);
//       }
//
//       return frame;
//     };
//   }
// })();


/* global require: false, console: false */
/* jshint unused: false */
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

    conf.paths['e2e-test'] = '/base/src/test/js/e2e';

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

      var originalSetUpAndRun = angular.scenario.setUpAndRun;
      angular.scenario.setUpAndRun = function(config) {
        amdSupport();
        // if (config.useamd) {
        //     amdSupport();
        // }
        return originalSetUpAndRun.apply(this, arguments);
      };

      function amdSupport() {
        var getFrame_ = angular.scenario.Application.prototype.getFrame_;

        /**
         *  This function should be added to angular-scenario to support amd. It overrides the load behavior to wait from
         *  the inner amd frame to be ready.
         */
        angular.scenario.Application.prototype.getFrame_ = function() {
          var frame = getFrame_.apply(this, arguments);
          var load = frame.load;

          frame.load = function(fn) {
            if (typeof fn === 'function') {
              angular.element(window).bind('message', function(e) {
                if (e.data && e.source === frame.prop('contentWindow') && e.data.type === 'loadamd') {
                  fn.call(frame, e);
                }
              });
              return this;
            }
            return load.apply(this, arguments);
          };

          return frame;
        };
      }

      require(tests, function() {
        // window.__karma__.start();
        return originalSetUpAndRun.apply(this, arguments);
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
