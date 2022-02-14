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

module.exports = function(config, lessConfig, pathConfig) {
  'use strict';

  var path = require('path');

  var file = {};

  var source = pathConfig.sourceDir + '/styles/styles.less';
  var destination = pathConfig.buildTarget + '/styles/styles.css';

  if (pathConfig.plugin) {
    source = pathConfig.sourceDir + '/styles.less';
    destination = pathConfig.buildTarget + '/plugin.css';
  }

  if (pathConfig.appName === 'commons-ui') {
    source = pathConfig.sourceDir + '/test-styles.less';
    destination = pathConfig.buildTarget + '/test-styles.css';
  }

  file[destination] = source;

  var ee = config.pkg.name === 'camunda-bpm-webapp-ee';
  var eePrefix = ee ? 'node_modules/camunda-bpm-webapp/' : '';
  var includePaths = [
    '<%= pkg.gruntConfig.commonsUiDir %>/lib/widgets',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/less',
    '<%= pkg.gruntConfig.commonsUiDir %>/resources/css',
    'node_modules',
    eePrefix + 'node_modules/',
    eePrefix + 'ui/common/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/styles',
    eePrefix + 'ui/' + pathConfig.appName + '/client/scripts'
  ];

  lessConfig[
    pathConfig.appName + (pathConfig.plugin ? '_plugin' : '') + '_styles'
  ] = {
    options: {
      paths: includePaths,

      compress: true,
      sourceMap: true,
      sourceMapURL: './' + path.basename(destination) + '.map',
      sourceMapFilename: destination + '.map'
    },
    files: file
  };

  if (pathConfig.appName === 'cockpit' && !pathConfig.plugin) {
    source = pathConfig.sourceDir + '/styles/styles-components.less';
    destination = pathConfig.buildTarget + '/styles/styles-components.css';
    file = {};
    file[destination] = source;

    lessConfig.cockpit_styles_components = {
      options: {
        paths: includePaths,

        compress: true,
        sourceMap: true,
        sourceMapURL: './' + path.basename(destination) + '.map',
        sourceMapFilename: destination + '.map'
      },
      files: file
    };
  }
};
