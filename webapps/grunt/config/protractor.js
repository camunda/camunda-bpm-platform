'use strict';


module.exports = function(config) {
  config = config || {};

  var seleniumInstall = require('grunt-seleniuminstall/tasks/seleniuminstall');

  config.pkg.standaloneSeleniumJar = seleniumInstall.standaloneSeleniumJar;
  config.pkg.chromeDriverPath = seleniumInstall.chromeDriverPath;

  return {
    options: {
      keepAlive: false, // If false, the grunt process stops when the test fails.
      noColor: false, // If true, protractor will not use colors in its output.
      args: {}
    },
    e2e: {
      options: {
        args: {
          seleniumServerJar: '<%= pkg.standaloneSeleniumJar() %>',

          chromeDriver: '<%= pkg.chromeDriverPath() %>',

          baseUrl: 'http://localhost:'+ config.connectPort,

          specs: [
            'test/e2e/**/*Spec.js'
          ],

          singleRun: true,

          capabilities: {
            browserName: 'chrome'
          },

          // // If you would like to run more than one instance of webdriver on the same
          // // tests, use multiCapabilities, which takes an array of capabilities.
          // // If this is specified, capabilities will be ignored.
          // multiCapabilities: [
          //   {
          //     browserName: 'chrome'
          //   },
          //   {
          //     browserName: 'phantomjs'
          //   }
          // ],

          framework: 'jasmine',

          // ----- Options to be passed to minijasminenode -----
          //
          // Options to be passed to Jasmine-node.
          // See the full list at https://github.com/juliemr/minijasminenode
          jasmineNodeOpts: {
            defaultTimeoutInterval: 15000, // Default time to wait in ms before a test fails.
            showColors: true, // Use colors in the command line report.
            // includeStackTrace: true, // If true, include stack traces in failures.
          }
        }
      }
    },
  };
};
