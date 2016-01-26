// module.exports = function(karma) {
//   karma.set({
//     frameworks: ['ng-scenario'],

//     files: [
//       // add require/amd support
//       'require-e2e-adapter.js',
//       '../e2e/cockpit-scenario.js'
//     ],

//     browsers: ['PhantomsJS'],

//     singleRun: true,
//     autoWatch: false,

//     junitReporter: {
//       outputFile: '../../../../target/failsafe-reports/e2e.xml',
//       suite: 'E2E'
//     },

//     // Proxying the original application due to same origin policy constraints
//     // https://github.com/karma-runner/karma/issues/179
//     urlRoot: '/__karma/',

//     proxies: {
//       '/': 'http://localhost:8080'
//     },

//     plugins: [
//       'karma-chrome-launcher',
//       'karma-firefox-launcher',
//       'karma-phantomjs-launcher',
//       'karma-ng-scenario'
//     ]
//   });
// };

module.exports = function(karma) {
  var conf = require('./../../../../src/main/webapp/require-conf');

  karma.set({
    basePath: '../../../..',

    frameworks: [
      'jasmine',
      'requirejs',
      'ng-scenario'
    ],

    files: [
      { pattern: 'target/webapp/**/*.js', included: false },
      { pattern: 'src/test/js/e2e/**/*.js', included: false },
      { pattern: 'src/test/js/lib/**/*.js', included: false },

      'src/test/js/config/require-e2e-adapter.js'
    ],

    browsers: ['PhantomJS'],

    singleRun: true,
    autoWatch: false,

    plugins: [
      'karma-ie-launcher',
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-requirejs',
      'karma-ng-scenario'
    ]
  });
};

