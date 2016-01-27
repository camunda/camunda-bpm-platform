'use strict';

var chai     = require('chai');
var promised = require('chai-as-promised');
chai.use(promised);
global.expect   = chai.expect;

var tested = process.env.TESTED || '*';

exports.config = {

  // The timeout for each script run on the browser. This should be longer
  // than the maximum time your application needs to stabilize between tasks.
  allScriptsTimeout: 11000,

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'args': ['incognito', 'disable-extensions', 'start-maximized', 'enable-crash-reporter-for-testing']
    },
    'loggingPrefs': {
      'browser': 'ALL'
    }
  },

  /*    multiCapabilities: [{
   'browserName': 'chrome'
   }, {
   'browserName': 'firefox'
   }],
   */

  // ----- What tests to run -----
  //
  // Spec patterns are relative to the location of the spec file. They may
  // include glob patterns.
  specs: [
    '{admin,tasklist,cockpit}/specs/' + tested + '-spec.js'
  ],

  // A base URL for your application under test. Calls to protractor.get()
  // with relative paths will be prepended with this.
  baseUrl: 'http://localhost:8080',

  // ----- The test framework -----
  //
  // Jasmine is fully supported as a test and assertion framework.
  // Mocha has limited beta support. You will need to include your own
  // assertion framework if working with mocha.
  framework: 'mocha',

/*
  // ----- Options to be passed to minijasminenode -----
  //
  // Options to be passed to Jasmine-node.
  // See the full list at https://github.com/juliemr/minijasminenode
  jasmineNodeOpts: {
    defaultTimeoutInterval: 15000, // Default time to wait in ms before a test fails.
    showColors: true, // Use colors in the command line report.
    includeStackTrace: true // If true, include stack traces in failures.
  }
  */

  mochaOpts: {
    timeout: 15000,
    colors: true,
    reporter: 'spec',
    slow: 3000
  }
};
