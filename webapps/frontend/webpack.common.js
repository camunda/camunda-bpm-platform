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

const HtmlWebPackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');

const path = require('path');
const webpack = require('webpack');

const {version} = require(path.resolve(__dirname, './package.json'));

module.exports = (_env, argv = {}) => {
  const eeBuild = !!argv.eeBuild;
  const devMode = !!argv.devMode;

  return {
    entry: {
      /* Cockpit */
      'app/cockpit/camunda-cockpit-bootstrap': {
        import: path.resolve(
          __dirname,
          'ui/cockpit/client/scripts/camunda-cockpit-bootstrap.js'
        )
      },
      'plugin/cockpit/app/plugin': {
        import: path.resolve(__dirname, 'ui/cockpit/plugins/cockpitPlugins.js')
      },

      /* Tasklist */
      'app/tasklist/camunda-tasklist-bootstrap': {
        import: path.resolve(
          __dirname,
          'ui/tasklist/client/scripts/camunda-tasklist-bootstrap.js'
        )
      },
      'plugin/tasklist/app/plugin': {
        import: path.resolve(__dirname, 'ui/tasklist/plugins/tasklistPlugins.js')
      },

      /* Admin */
      'app/admin/camunda-admin-bootstrap': {
        import: path.resolve(
          __dirname,
          'ui/admin/client/scripts/camunda-admin-bootstrap.js'
        )
      },
      'plugin/admin/app/plugin': {
        import: path.resolve(__dirname, 'ui/admin/plugins/adminPlugins.js')
      },

      /* Welcome */
      'app/welcome/camunda-welcome-bootstrap': {
        import: path.resolve(
          __dirname,
          'ui/welcome/client/scripts/camunda-welcome-bootstrap.js'
        )
      }
    },
    stats: {
      errorDetails: true
    },
    output: {
      library: '[name]',
      libraryTarget: 'umd',
      filename: `[name].js?bust=${version}`,
      assetModuleFilename: `assets/[name][ext]?bust=${version}`,
      path: path.resolve(__dirname, 'target/webapp')
    },
    resolve: {
      fallback: {
        fs: false
      },
      extensions: ['.js', '.less'],
      alias: {
        'camunda-commons-ui': path.resolve(__dirname, 'camunda-commons-ui'),
        ui: path.resolve(__dirname, 'ui'),
        'camunda-bpm-sdk-js': path.resolve(__dirname, 'camunda-bpm-sdk-js'),
        'cam-common': path.resolve(__dirname, 'ui/common/scripts/module'),
        jquery: path.resolve(__dirname, 'node_modules/jquery'),
        'core-js': path.resolve(__dirname, 'node_modules/core-js'),
        angular: path.resolve(__dirname, 'node_modules/angular'), // avoid loading angular twice
        // prevent ids from being tree shaken (transitive dependency of form-js)
        ids: path.resolve(__dirname, 'node_modules/ids/dist/index.umd.js'),
        // prevent`preact-markup`being tree shaken (transitive dependency of form-js
        'preact-markup': path.resolve(
          __dirname,
          'node_modules/preact-markup/dist/preact-markup.umd.js'
        ),
        // prevent `flatpickr` being tree shaken (transitive dependency of form-js)
        flatpickr: path.resolve(
          __dirname,
          'node_modules/flatpickr/dist/flatpickr.js'
        )
      }
    },
    module: {
      rules: [
        {
          test: /(\.html)|(\.svg)$/,
          resourceQuery: /raw/,
          type: 'asset/source'
        },
        {
          test: /\.less$/i,
          use: [
            MiniCssExtractPlugin.loader,
            'css-loader',
            {
              loader: 'less-loader',
              options: {
                lessOptions: {
                  globalVars: {
                    'ce-banner-height': eeBuild ? '0' : '20px'
                  }
                }
              }
            }
          ]
        },
        {
          test: /\.js$/,
          exclude: /node_modules/,
          use: ['babel-loader']
        }
      ]
    },
    plugins: [
      new HtmlWebPackPlugin({
        minify: false,
        inject: false,
        appName: 'cockpit',
        template: path.resolve(__dirname, 'ui/cockpit/client/scripts/index.html'),
        filename: 'app/cockpit/index.html',
        chunks: ['app/cockpit/camunda-cockpit-bootstrap'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        publicPath: '$APP_ROOT',
        version
      }),
      new HtmlWebPackPlugin({
        minify: false,
        inject: false,
        appName: 'tasklist',
        template: path.resolve(__dirname, 'ui/tasklist/client/index.html'),
        filename: 'app/tasklist/index.html',
        chunks: ['app/tasklist/camunda-tasklist-bootstrap'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        publicPath: '$APP_ROOT',
        version
      }),
      new HtmlWebPackPlugin({
        minify: false,
        inject: false,
        appName: 'admin',
        template: path.resolve(__dirname, 'ui/admin/client/scripts/index.html'),
        filename: 'app/admin/index.html',
        chunks: ['app/admin/camunda-admin-bootstrap'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        publicPath: '$APP_ROOT',
        version
      }),
      new HtmlWebPackPlugin({
        minify: false,
        inject: false,
        appName: 'welcome',
        template: path.resolve(__dirname, 'ui/welcome/client/scripts/index.html'),
        filename: 'app/welcome/index.html',
        chunks: ['app/welcome/camunda-welcome-bootstrap'],
        favicon: path.resolve(__dirname, 'ui/common/images/favicon.ico'),
        publicPath: '$APP_ROOT',
        version
      }),
      new MiniCssExtractPlugin({
        // both options are optional, similar to the same options in webpackOptions.output
        filename: `[name].css?bust=${version}`,
        chunkFilename: `[id].css?bust=${version}`
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: path.resolve(__dirname, 'public'),
            to: './'
          },
          {
            from: path.resolve(
              __dirname,
              'node_modules/requirejs-angular-define/dist/ngDefine.js'
            ),
            to: './lib',
            noErrorOnMissing: true
          },
          {
            from: path.resolve(
              __dirname,
              '../../node_modules/requirejs-angular-define/dist/ngDefine.js'
            ),
            to: './lib',
            noErrorOnMissing: true
          }
        ]
      }),
      new webpack.DefinePlugin({
        CAMUNDA_VERSION: `'${version}'`,
        CAMUNDA_EDITION: eeBuild ? '"EE"' : '"CE"',
        DEV_MODE: devMode,
        _import: 'function(filePath) { return import(filePath) }' // dynamic import workaround
      }),
      new webpack.ProvidePlugin({
        CAMUNDA_EDITION: eeBuild ? '"EE"' : '"CE"',
        DEV_MODE: devMode
      }),
      new ESLintPlugin()
    ],
    optimization: {
      // Avoids that imported modules are initialized for each chunk separately
      runtimeChunk: {
        name: 'lib/runtime'
      }
    }
  };
};
