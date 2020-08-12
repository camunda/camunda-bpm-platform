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

  var publishedFiles = [
    'camunda-bpm-sdk-angular.js',
    'camunda-bpm-sdk.js',
    'camunda-bpm-sdk.min.js'
  ];

  return {
    options: {
      main: 'camunda-bpm-sdk.js',
      endpoint: 'git@github.com:camunda/bower-camunda-bpm-sdk-js.git',
      packageName: 'camunda-bpm-sdk-js',
      commitMessage: 'chore(project): release <%= pkg.version %>',
      tagMessage: 'chore(project): release <%= pkg.version %>',
      push: !config.dryRun
    },
    release: {
      options: {
        branchName: 'master',
        createTag: true
      },
      files: [
        {
          expand: true,
          cwd: 'dist/',
          src: publishedFiles
        }
      ]
    },
    snapshot: {
      options: {
        branchName: '<%= pkg.version %>',
        createTag: false,
        forcePush: true
      },
      files: [
        {
          expand: true,
          cwd: 'dist/',
          src: publishedFiles
        }
      ]
    }
  };
};
