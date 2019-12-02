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

  browserifyConfig.admin_scripts = {
    options: {
      browserifyOptions: {
        standalone: 'CamundaAdminUi',
        debug: true
      },
      watch: true,
      transform: [[
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
      ['brfs', {global: true}]
      ],
      postBundleCB: function(err, src, next) {
        if(err) {
          console.log(err);
        }


        var buildMode = config.grunt.config('buildMode');
        var livereloadPort = config.grunt.config(
          'pkg.gruntConfig.livereloadPort'
        );

        if(err) console.log(err);

        if (buildMode !== 'prod' && livereloadPort) {
          config.grunt.log.writeln(
            'Enabling livereload for admin on port: ' + livereloadPort
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
      './<%= pkg.gruntConfig.adminSourceDir %>/scripts/camunda-admin-ui.js'
    ],
    dest: '<%= pkg.gruntConfig.adminBuildTarget %>/scripts/camunda-admin-ui.js'
  };

  browserifyConfig.admin_plugins = {
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
              moment: 'moment',
              'moment-precise-range-plugin': 'moment-precise-range-plugin',
              events: 'events',
              'cam-common': 'cam-common',
              lodash: 'lodash'
            }
          }
        ],
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
        ['brfs', {global: true}]
      ],
      browserifyOptions: {
        standalone: 'AdminPlugins',
        debug: true
      }
    },
    src: [
      './<%= pkg.gruntConfig.pluginSourceDir %>/admin/plugins/adminPlugins.js'
    ],
    dest: '<%= pkg.gruntConfig.pluginBuildTarget %>/admin/app/plugin.js'
  };
};
