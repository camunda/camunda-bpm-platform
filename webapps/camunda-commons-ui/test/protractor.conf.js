/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var chai     = require('chai');
var promised = require('chai-as-promised');
chai.use(promised);
global.expect   = chai.expect;

var tested = process.env.TESTED || '*';

exports.config = {

  // directConnect: true,
  // The timeout for each script run on the browser. This should be longer
  // than the maximum time your application needs to stabilize between tasks.
  allScriptsTimeout: 61000,

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'args': ['start-maximized', 'enable-crash-reporter-for-testing']
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
    '../lib/widgets/' + tested + '/test/cam-widget-' + tested + '.spec.js'
  ],

  // A base URL for your application under test. Calls to protractor.get()
  // with relative paths will be prepended with this.
  baseUrl: 'http://localhost:8070',

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
    timeout: 60000,
    colors: true,
    reporter: 'spec',
    slow: 3000
  }
};
