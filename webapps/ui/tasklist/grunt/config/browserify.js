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

module.exports = function(config, browserifyConfig) {
  'use strict';

  browserifyConfig.tasklist_scripts = {
    options: {
      browserifyOptions: {
        standalone: 'CamundaTasklistUi',
        debug: true
      },
      watch: true,
      transform: [
      [
        'babelify',
        {
          global: true,
          compact: false,
          ignore: [/node_modules(?:\/|\\{1,2})core-js/],
          presets: [
            [
              '@babel/preset-env',
              {
                targets:
                  'last 1 chrome version, last 1 firefox version, last 1 edge version',
                forceAllTransforms: true,
                useBuiltIns: 'usage',
                corejs: 3
              }
            ]
          ]
        }
      ],
      ['brfs', {global: true}]],
      postBundleCB: function(err, src, next) {
        console.log('post bundling', err);

        var buildMode = config.grunt.config('buildMode');
        var livereloadPort = config.grunt.config(
          'pkg.gruntConfig.livereloadPort'
        );
        if (buildMode !== 'prod' && livereloadPort) {
          config.grunt.log.writeln(
            'Enabling livereload for tasklist on port: ' + livereloadPort
          );
          //var contents = grunt.file.read(data.path);
          var contents = src.toString();

          contents = contents
            .replace(/\/\* live-reload/, '/* live-reload */')
            .replace(/LIVERELOAD_PORT/g, livereloadPort);

          next(err, new Buffer(contents));
        } else {
          next(err, src);
        }
      }
    },
    src: [
      './<%= pkg.gruntConfig.tasklistSourceDir %>/scripts/camunda-tasklist-ui.js'
    ],
    dest:
      '<%= pkg.gruntConfig.tasklistBuildTarget %>/scripts/camunda-tasklist-ui.js'
  };

  browserifyConfig.tasklist_plugins = {
    options: {
      watch: true,
      transform: [
        [
          'exposify',
          {
            expose: {
              angular: 'angular',
              jquery: 'jquery',
              'camunda-commons-ui': 'camunda-commons-ui',
              'camunda-bpm-sdk-js': 'camunda-bpm-sdk-js',
              'angular-data-depend': 'angular-data-depend',
              'cam-common': 'cam-common',
              lodash: 'lodash'
            }
          }
        ],
        ['babelify',
        {
          global: true,
          compact: false,
          ignore: [/node_modules(?:\/|\\{1,2})core-js/],
          presets: [
            [
              '@babel/preset-env',
              {
                targets:
                  'last 1 chrome version, last 1 firefox version, last 1 edge version',
                forceAllTransforms: true,
                useBuiltIns: 'usage',
                corejs: 3
              }
            ]
          ]
        }],
        ['brfs', {global: true}]
      ],
      browserifyOptions: {
        standalone: 'TasklistPlugins',
        debug: true
      }
    },
    src: [
      './<%= pkg.gruntConfig.pluginSourceDir %>/tasklist/plugins/tasklistPlugins.js'
    ],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/tasklist/app/plugin.js'
  };
};
