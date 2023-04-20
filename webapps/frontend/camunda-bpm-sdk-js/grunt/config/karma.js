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

// Usage:
//
// ```sh
// grunt karma
// ```
//
// or
//
// ```sh
// BROWSERS=PhantomJS TESTED=angular-forms-base grunt karma:dev-form-angularjs
// ```
module.exports = function() {
  'use strict';

  var singleRun = false;
  var tested = process.env.TESTED || '*';
  var browsers = process.env.BROWSERS ? process.env.BROWSERS.split(',') : ['Chrome'];

  return {
    options: {
      singleRun: singleRun,
      autoWatch: !singleRun,

      frameworks: ['browserify', 'mocha', 'chai'],

      browsers: [
        'Chrome',
        'Firefox',
        'PhantomJS'
      ],

      preprocessors: {
        'test/karma/**/*-spec.js': [ 'browserify' ]
      },

      files: []
    },

    'dev-form': {
      options: {

        browsers: browsers,

        files: [
          {pattern: 'test/jquery-2.1.1.min.js', included: true},
          {pattern: 'test/karma/forms/**/*.html', included: false},
          {pattern: 'test/karma/forms/**/' + tested + '-spec.js', included: true}
        ]
      }
    },

    'dev-form-angularjs': {
      options: {

        browsers: browsers,

        files: [
          {pattern: 'test/jquery-2.1.1.min.js', included: true},

          {pattern: 'test/karma/forms-angularjs/**/*.html', included: false},
          {pattern: 'test/karma/forms-angularjs/**/' + tested + '-spec.js', included: true}
        ]
      }
    }
  };
};
