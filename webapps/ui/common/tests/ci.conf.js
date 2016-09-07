'use strict';

var chai     = require('chai');
var promised = require('chai-as-promised');

chai.use(promised);
global.expect   = chai.expect;

process.env.PROSHOT_DIR = './target/screenshots';
process.env.multi = 'xunit-file=- mocha-proshot=-';

var tested = process.env.TESTED || '*';

exports.config = {

  // The timeout for each script run on the browser. This should be longer
  // than the maximum time your application needs to stabilize between tasks.
  allScriptsTimeout: 15000,

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome',
    'loggingPrefs': {
      'browser': 'ALL'
    }
  },

  // ----- What tests to run -----
  //
  // Spec patterns are relative to the location of the spec file. They may
  // include glob patterns.
  specs: [
    '../../{admin,tasklist,cockpit,welcome}/tests/specs/' + tested + '-spec.js'
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

  // ----- Options to be passed to minijasminenode -----
  //
  // Options to be passed to Mocha-node.
  // See the full list at https://github.com/juliemr/minijasminenode

  mochaOpts: {
    timeout: 15000,
    colors: false,
    reporter: 'mocha-multi',
    slow: 3000
  }
};
