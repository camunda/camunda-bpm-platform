'use strict';

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '../../',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['mocha', 'browserify'],


    // list of files / patterns to load in the browser
    files: [
      'common/unit-tests/expose.js',
      '**/unit-tests/*.spec.js'
    ],


    // list of files to exclude
    exclude: [],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      'common/unit-tests/expose.js': ['browserify'],
      '**/unit-tests/*.spec.js': ['browserify']
    },

    browserify: {
      debug: true,
      transform: [
        'brfs',
        [ 'exposify',
          {
            expose: {
              'angular': 'angular',
              'jquery': 'jquery',
              'camunda-commons-ui': 'camunda-commons-ui',
              'camunda-bpm-sdk-js': 'camunda-bpm-sdk-js',
              'angular-data-depend': 'angular-data-depend',
              'moment': 'moment',
              'events': 'events',
              'cam-common': 'cam-common'
            }
          }
        ]
      ]
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['mocha', 'junit'],

    junitReporter: {
      outputDir: '../test-results/js/results'
    },


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: [
      'Chrome_without_security'
      // 'Firefox',
      // 'IE'
    ],

    customLaunchers: {
      Chrome_without_security: {
        base: 'Chrome',
        flags: ['--disable-web-security']
      }
    }
  });
};
