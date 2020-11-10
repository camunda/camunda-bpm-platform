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

  watchConf.admin_assets = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.adminSourceDir %>/{fonts,images}/**/*',
      '<%= pkg.gruntConfig.adminSourceDir %>/scripts/index.html',
      '<%= pkg.gruntConfig.adminSourceDir %>/scripts/favicon.ico'
    ],
    tasks: ['copy:admin_assets', 'copy:admin_index']
  };

  watchConf.admin_styles = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.adminSourceDir %>/styles/**/*.{css,less}',
      '<%= pkg.gruntConfig.adminSourceDir %>/scripts/**/*.{css,less}'
    ],
    tasks: ['less:admin_styles']
  };

  watchConf.admin_plugin_styles = {
    options: options,
    files: [
      '<%= pkg.gruntConfig.pluginSourceDir %>/admin/plugins/**/*.{css,less}'
    ],
    tasks: ['less:admin_plugin_styles']
  };

  watchConf.admin_scripts_lint = {
    options: options,
    files: ['<%= pkg.gruntConfig.adminSourceDir %>/scripts/**/*.js'],
    tasks: ['newer:eslint:admin_scripts']
  };

  watchConf.admin_plugins_lint = {
    options: options,
    files: ['<%= pkg.gruntConfig.pluginSourceDir %>/admin/plugins/**/*.js'],
    tasks: ['newer:eslint:admin_plugins']
  };

  watchConf.admin_dist = {
    options: {
      livereload: config.livereloadPort || false
    },
    files: [
      '<%= pkg.gruntConfig.adminBuildTarget %>/**/*.{css,html,js}',
      '<%= pkg.gruntConfig.pluginBuildTarget %>/admin/**/*.{css,html,js}'
    ]
  };
};
