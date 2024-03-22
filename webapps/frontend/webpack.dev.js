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

const path = require('path');
const {merge} = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = (_env, argv = {}) => {
  const eeBuild = !!argv.eeBuild;

  const commonConfig = require(path.resolve(
    __dirname,
    './webpack.common.js'
  ))(_env, {...argv, eeBuild, devMode: true});

  const addEngines = engines => {
    return engines.reduce((acc, engine) => {
      acc[`/camunda/app/*/${engine}/`] = {
        target: 'http://localhost:8081/',
        pathRewrite: path => {
          return path.replace(`/${engine}`, '').replace('/camunda', '');
        }
      };
      acc[`/camunda/app/*/${engine}/setup/`] = {
        target: 'http://localhost:8081/',
        pathRewrite: path => {
          return path
            .replace(`/${engine}`, '')
            .replace('/camunda', '')
            .replace('/setup', '');
        }
      };
      return acc;
    }, {});
  };

  const developmentConfig = {
    output: {
      publicPath: '/'
    },
    entry: {
      client: 'webpack-dev-server/client?http://localhost:8081?live-reload=true'
    },
    devtool: 'source-map',
    devServer: {
      port: 8081,
      static: {
        directory: path.resolve(__dirname, './public'),
        publicPath: '/app'
      },
      https: false,
      proxy: {
        '/api': {
          target: 'http://localhost:8080/camunda/api',
          logLevel: 'debug',
          pathRewrite: {
            '^/api': ''
          }
        },
        '/camunda-welcome': {
          target: 'http://localhost:8080/',
          logLevel: 'debug'
        },
        ...addEngines(['default', 'engine2', 'engine3']),
        '/camunda/*': {
          target: 'http://localhost:8081/',
          logLevel: 'debug',
          pathRewrite: path => {
            return path.replace('/camunda', '');
          }
        },
        '/camunda/api/*': {
          target: 'http://localhost:8081/',
          logLevel: 'debug',
          pathRewrite: path => {
            return path.replace('/camunda', '');
          }
        }
      },
      open: ['/camunda/app/cockpit/default/']
    }
  };

  const merged = merge(commonConfig, developmentConfig);
  merged.plugins.forEach(plugin => {
    const eeApps = ['admin', 'cockpit'];
    function getPluginDeps(appName) {
      const pluginDependencies = [];
      if (appName !== 'welcome') {
        pluginDependencies.push({
          ngModuleName: `${appName}.plugin.${appName}Plugins`,
          requirePackageName: `${appName}-plugin-${appName}Plugins`
        });
        if (eeBuild && eeApps.includes(appName)) {
          pluginDependencies.push({
            ngModuleName: `${appName}.plugin.${appName}EE`,
            requirePackageName: `${appName}-plugin-${appName}EE`
          });
        }
      }
      return JSON.stringify(pluginDependencies);
    }

    function getPluginPackages(appName) {
      const pluginPackages = [];
      if (appName !== 'welcome') {
        pluginPackages.push({
          name: `${appName}-plugin-${appName}Plugins`,
          location: `/plugin/${appName}/app/`,
          main: 'plugin.js'
        });
        if (eeBuild && eeApps.includes(appName)) {
          pluginPackages.push({
            name: `${appName}-plugin-${appName}EE`,
            location: `/plugin/${appName}EE/app/`,
            main: 'plugin.js'
          });
        }
      }
      return JSON.stringify(pluginPackages);
    }

    if (plugin instanceof HtmlWebpackPlugin) {
      const options = plugin.options;
      plugin.options = {
        ...options,
        publicPath: '/camunda',
        appRoot: '/camunda',
        appBase: `/camunda/app/${options['appName']}/{ENGINE}/`,
        pluginDeps: getPluginDeps(options['appName']),
        pluginPackages: getPluginPackages(options['appName'])
      };
    }
  });
  return merged;
};
