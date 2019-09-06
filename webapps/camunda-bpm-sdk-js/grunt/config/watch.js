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
  'use strict';
  config = config || {};

  return {
    options: {
      livereload: false
    },

    sources: {
      files: [
        'lib/**/*.js'
      ],
      tasks: [
        'newer:eslint',
        'browserify',
        'copy:builds'
      ]
    },

    doc: {
      files: [
        'doc/**/*.md',
        'lib/**/*.js'
      ],
      tasks: [
        'newer:jshint',
        'jsdoc'
      ]
    },

    mochacli: {
      files: [
        'lib/**/*.js',
        'test/client/**/*Spec.js'
      ],
      tasks: [
        'mochacli'
      ]
    },

    karma: {
      files: [
        'dist/camunda-bpm-sdk.js',
        'test/karma/**/*.js'
      ],
      tasks: [
        'karma:watched'
      ]
    }
  };
};
