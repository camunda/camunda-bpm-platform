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

module.exports = function(config) {

  return {
    options: {
      livereload: false
    },

    assets: {
      files: [
        'client/{fonts,images}/**/*',
        'client/index.html',
        'client/favicon.ico'
      ],
      tasks: [
        'newer:copy:assets'
      ]
    },

    styles: {
      files: [
        'client/styles/**/*.{css,less}',
        'client/scripts/*/*.{css,less}'
      ],
      tasks: [
        'less'
      ]
    },

    scripts: {
      files: [
        'grunt/config/requirejs.js',
        'client/tasklist.html',
        'client/scripts/**/*.{js,html}'
      ],
      tasks: [
        'newer:jshint:scripts',
        // 'requirejs:dependencies',
        'requirejs:scripts'
      ]
    },

    sdk: {
      files: [
        'node_modules/camunda-bpm-sdk-js/dist/**/*.js'
      ],
      tasks: [
        'copy:sdk',
        'requirejs:scripts'
      ]
    },

    integrationTest: {
      files: [
        'grunt/config/karma.js',
        'test/integration/main.js',
        'test/integration/**/*Spec.js'
      ],
      tasks: [
        'karma:integration'
      ]
    }
  };
};
