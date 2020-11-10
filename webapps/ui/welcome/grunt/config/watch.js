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

module.exports = function(config, watchConf) {
  'use strict';

  var options = {
    livereload: false
  };

  watchConf.welcome_assets = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.welcomeSourceDir %>/{fonts,images}/**/*',
      '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/index.html',
      '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/favicon.ico'
    ],
    tasks: ['copy:welcome_assets', 'copy:welcome_index']
  };

  watchConf.welcome_styles = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.welcomeSourceDir %>/styles/**/*.{css,less}',
      '<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/**/*.{css,less}'
    ],
    tasks: ['less:welcome_styles']
  };

  watchConf.welcome_plugin_styles = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/welcome/plugins/**/*.{css,less}'
    ],
    tasks: ['less:welcome_plugin_styles']
  };

  watchConf.welcome_scripts_lint = {
    options: options,
    files: ['<%= pkg.gruntConfig.welcomeSourceDir %>/scripts/**/*.js'],
    tasks: ['newer:eslint:welcome_scripts']
  };

  watchConf.welcome_plugins_lint = {
    options: options,
    files: ['<%= pkg.gruntConfig.pluginSourceDir %>/welcome/plugins/**/*.js'],
    tasks: ['newer:eslint:welcome_plugins']
  };

  watchConf.welcome_dist = {
    options: {
      livereload: config.livereloadPort || false
    },
    files: [
      '<%= pkg.gruntConfig.welcomeBuildTarget %>/**/*.{css,html,js}',
      '<%= pkg.gruntConfig.pluginBuildTarget %>/welcome/**/*.{css,html,js}'
    ]
  };
};
