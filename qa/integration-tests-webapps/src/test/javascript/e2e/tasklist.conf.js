/*
    configuration file for cockpit testing

    to run test start:
    1. tasklist
    2. webdriver-manager start
    3. protractor conf-file-name.js
*/

exports.config = {
  
  // ----- How to setup Selenium -----
  //   
  // The address of a running selenium server.
  seleniumAddress: 'http://localhost:4444/wd/hub',

  // The timeout for each script run on the browser. This should be longer
  // than the maximum time your application needs to stabilize between tasks.
  allScriptsTimeout: 11000,  

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome'
  },

  // ----- What tests to run -----
  //
  // Spec patterns are relative to the location of the spec file. They may
  // include glob patterns.
  specs: ['tasklist/*Spec.js'],

  // A base URL for your application under test. Calls to protractor.get()
  // with relative paths will be prepended with this.
  baseUrl: 'http://localhost:8080',

  // ----- The test framework -----
  //
  // Jasmine is fully supported as a test and assertion framework.
  // Mocha has limited beta support. You will need to include your own
  // assertion framework if working with mocha.
  framework: 'jasmine',

  // ----- Options to be passed to minijasminenode -----
  //
  // Options to be passed to Jasmine-node.
  // See the full list at https://github.com/juliemr/minijasminenode
  jasmineNodeOpts: {
    defaultTimeoutInterval: 15000, // Default time to wait in ms before a test fails.
    showColors: true, // Use colors in the command line report.        
    includeStackTrace: true, // If true, include stack traces in failures.
  }
};