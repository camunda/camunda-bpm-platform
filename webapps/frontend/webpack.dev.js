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
const webpack = require('webpack');

const commonConfig = require(path.resolve(__dirname, './webpack.common.js'));

const { merge } = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = (_env, argv = {}) => {
  const eeBuild = !!argv.eeBuild;
  const webapps = [
    {
      name: 'cockpit',
      indexFile: /ui\/cockpit\/client\/scripts\/index\.html$/
    },
    {
      name: 'admin',
      indexFile: /ui\/admin\/client\/scripts\/index\.html$/
    },
    {name: 'tasklist', indexFile: /ui\/tasklist\/client\/index\.html$/},
    {
      name: 'welcome',
      indexFile: /ui\/welcome\/client\/scripts\/index\.html$/
    }
  ];

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

  const eeApps = ['admin', 'cockpit'];
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
        '/camunda-welcome/': {
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
    },
    module: {
      rules: [
        ...webapps.map(({name, indexFile}) => {
          const pluginDependencies = [];
          if (name !== 'welcome') {
            pluginDependencies.push({
              ngModuleName: `${name}.plugin.${name}Plugins`,
              requirePackageName: `${name}-plugin-${name}Plugins`
            });
            if (eeBuild && eeApps.includes(name)) {
              pluginDependencies.push({
                ngModuleName: `${name}.plugin.${name}EE`,
                requirePackageName: `${name}-plugin-${name}EE`,
              });
            }
          }
          const pluginPackages = [];
          if (name !== 'welcome') {
            pluginPackages.push({
              name: `${name}-plugin-${name}Plugins`,
              location: `/plugin/${name}/app/`,
              main: 'plugin.js'
            });
            if (eeBuild && eeApps.includes(name)) {
              pluginPackages.push({
                name: `${name}-plugin-${name}EE`,
                location: `/plugin/${name}EE/app/`,
                main: 'plugin.js',
              });
            }
          }

          return {
            test: indexFile,
            exclude: /node_modules/,
            loader: 'string-replace-loader',
            options: {
              multiple: [
                {search: /\$APP_ROOT/g, replace: '/camunda'},
                {search: /\$BASE/g, replace: `/camunda/app/${name}/{ENGINE}/`},
                {
                  search: /\$PLUGIN_DEPENDENCIES/g,
                  replace: JSON.stringify(pluginDependencies)
                },
                {
                  search: /\$PLUGIN_PACKAGES/g,
                  replace: JSON.stringify(pluginPackages)
                }
              ]
            }
          };
        })
      ]
    },
    plugins: [
      new webpack.DefinePlugin({
        // define custom global variables
        DEV_MODE: true
      }),
      new webpack.ProvidePlugin({
        DEV_MODE: true
      })
    ]
  };

  const merged = merge(commonConfig, developmentConfig);
  merged.plugins.forEach(plugin => {
    if (plugin instanceof HtmlWebpackPlugin) {
      plugin.userOptions.publicPath = '/camunda';
    }
  });
  return merged;
};
