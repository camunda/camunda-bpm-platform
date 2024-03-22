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

const {merge} = require('webpack-merge');
const TerserPlugin = require('terser-webpack-plugin');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const fs = require('fs');

class HtmlAdditionalAttributesPlugin {
  apply(compiler) {
    compiler.hooks.compilation.tap(
      HtmlAdditionalAttributesPlugin.name,
      compilation => {
        HtmlWebPackPlugin.getHooks(compilation).alterAssetTags.tap(
          HtmlAdditionalAttributesPlugin.name,
          config => {
            config.assetTags.scripts.forEach(script => {
              script.attributes['$CSP_NONCE'] = true;
              script.attributes.charset = 'UTF-8';
            });
            return config;
          }
        );
      }
    );
  }
}

module.exports = (_env, argv = {}) => {
  const eeBuild = !!argv.eeBuild;

  const commonConfig = require(path.resolve(
    __dirname,
    './webpack.common.js'
  ))(_env, {...argv, eeBuild});

  const productionConfig = {
    output: {
      clean: true
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          include: /node_modules/,
          use: [path.resolve(__dirname, './scripts/license-header-loader.js')]
        }
      ]
    },
    plugins: [
      new webpack.BannerPlugin(
        fs.readFileSync('./license-banner.txt', 'utf-8')
      ),
      new HtmlAdditionalAttributesPlugin()
    ],
    optimization: {
      minimize: true,
      minimizer: [
        new TerserPlugin({
          extractComments: {
            condition: (astNode, comment) => {
              return (
                /^\**!/i.test(comment.value) || // include license headers with a leading ! (e.g., moment.js)
                (/@license|@preserve|@lic|@cc_on/i.test(comment.value) &&
                  'comment2' === comment.type)
              );
            },
            banner: licenseFile => {
              return `For license information, please see ${licenseFile}`;
            }
          },
          exclude: [/scripts\/config\.js/, /lib\/globalize\.js/]
        })
      ],
      // Bundle all third-party modules into the lib/deps.js bundle
      splitChunks: {
        cacheGroups: {
          commons: {
            test: /[\\/]node_modules[\\/]/,
            name: 'lib/deps',
            chunks: 'all'
          }
        }
      }
    }
  };

  const merged = merge(commonConfig, productionConfig);

  merged.plugins.forEach(plugin => {
    if (plugin instanceof HtmlWebPackPlugin) {
      plugin.options = {
        ...plugin.options,
        appRoot: '$APP_ROOT',
        appBase: '$BASE',
        pluginDeps: '$PLUGIN_DEPENDENCIES',
        pluginPackages: '$PLUGIN_PACKAGES'
      };
    }
  });
  return merged;
};
