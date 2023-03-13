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

var fs = require('fs');
var through = require('through2');
const path = require('path');

var included = [
  '@bpmn-io/dmn-migrate',
  '@bpmn-io/form-js-editor',
  '@bpmn-io/form-js-viewer',
  '@bpmn-io/form-js',
  'angular-animate',
  'angular-cookies',
  'angular-data-depend',
  'angular-loader',
  'angular-mocks',
  'angular-moment',
  'angular-resource',
  'angular-route',
  'angular-sanitize',
  'angular-touch',
  'angular-translate',
  'angular-ui-bootstrap',
  'angular',
  'bootstrap',
  'bpmn-js',
  'bpmn-js/lib/NavigatedViewer',
  'camunda-bpm-sdk-js',
  'camunda-bpm-sdk-js/lib/angularjs/index',
  'camunda-dmn-js',
  'clipboard',
  'cmmn-js',
  'cmmn-js/lib/Viewer',
  'cmmn-js/lib/NavigatedViewer',
  'core-js',
  'dmn-js-decision-table/lib/Viewer',
  'dmn-js-drd/lib/NavigatedViewer',
  'dmn-js-literal-expression/lib/Viewer',
  'dmn-js-shared/lib/base/Manager',
  'dmn-js-shared/lib/util/DiUtil',
  'dmn-js-shared/lib/util/ModelUtil',
  'dom4',
  'events',
  'fast-xml-parser',
  'jquery',
  'jquery/external/sizzle/dist/sizzle',
  'jquery-ui/ui/data',
  'jquery-ui/ui/plugin',
  'jquery-ui/ui/safe-active-element',
  'jquery-ui/ui/safe-blur',
  'jquery-ui/ui/scroll-parent',
  'jquery-ui/ui/version',
  'jquery-ui/ui/widget',
  'jquery-ui/ui/widgets/draggable',
  'jquery-ui/ui/widgets/mouse',
  'lodash',
  'moment',
  'mousetrap',
  'q',
  'superagent'
];

module.exports = function(grunt, dirname, licensebookConfig) {
  grunt.registerMultiTask('ensureLibs', function() {
    var done = this.async();

    var browserifyOptions = {
      transform: [
        [
          '@browserify/envify',
          {
            global: true,
            NODE_ENV: 'production'
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
        ]
      ],
      paths: [
        'node_modules',
        'node_modules/camunda-bpm-webapp',
        'node_modules/camunda-bpm-webapp/node_modules',
        '../../../node_modules',
        '../../../',
        './'
      ]
    };
    var persistifyOptions = {
      recreate: true,
      cacheId: 'deps'
    };
    var dest = __dirname + '/../../cache/deps.js';
    var cacheDest = __dirname + '/../../cache/deps.json';

    var b = require(dirname + '/node_modules/persistify')(
      browserifyOptions,
      persistifyOptions
    );

    const addMissingLicenseHeaders = row => {
      // This fix ensures windows compatibility
      // See https://github.com/camunda/camunda-bpm-platform/issues/2824
      const rowFile = row.file.replace(/\\/g, '/');
      if (
        rowFile &&
        !rowFile.endsWith('.json') &&
        !/@license|@preserve|@lic|@cc_on|^\/\**!/i.test(row.source)
      ) {
        let pkg = null;
        if (rowFile.includes('node_modules')) {
          pkg = rowFile.replace(
            /^(.*)node_modules\/(@[a-z-\d.]+\/[a-z-\d.]+)?([a-z-\d.]+)?(.*)$/,
            (match, p1, p2, p3) => p2 || p3
          );
        } else if (!rowFile.includes('camunda-bpm-sdk-js')) {
          pkg = rowFile.replace(
            /^(@[a-z-\d.]+\/[a-z-\d.]+)?([a-z-\d.]+)?(.*)$/,
            (match, p1, p2) => p2 || p1
          );
        }

        if (pkg) {
          const nodeModulesPath = rowFile.split(pkg)[0];
          let packagePath =  nodeModulesPath + pkg;
          if (!nodeModulesPath) {
            packagePath = `${process.cwd()}/node_modules/${pkg}`;
            if (!fs.existsSync(packagePath)) {
              packagePath = `${process.cwd()}/node_modules/camunda-bpm-webapp/node_modules/${pkg}`;
            }
          }

          let licenseInfo = null;
          try {
            licenseInfo = fs.readFileSync(`${packagePath}/LICENSE`, 'utf8');
          } catch (e) {
            try {
              licenseInfo = fs.readFileSync(
                `${packagePath}/LICENSE.md`,
                'utf8'
              );
            } catch (e) {
              try {
                licenseInfo = fs.readFileSync(
                  `${packagePath}/LICENSE-MIT.txt`,
                  'utf8'
                );
              } catch (e) {
                try {
                  licenseInfo = fs.readFileSync(
                    `${packagePath}/LICENSE.txt`,
                    'utf8'
                  );
                } catch (e) {
                  console.log(`${pkg} has no license file. 🤷‍`);
                }
              }
            }
          }

          let packageJsonPath = require.resolve(`${packagePath}/package.json`);
          const {version, license} = require(packageJsonPath);
          if (licenseInfo) {
            row.source = `/*!\n@license ${pkg}@${version}\n${licenseInfo}*/\n${row.source}`;
          } else if (license) {
            console.log(`${pkg} has a "license" property. 🤷‍`);
            row.source = `/*! @license ${pkg}@${version} (${license}) */\n${row.source}`;
          }
        }
      }
    };

    b.pipeline.get('deps').push(
      through.obj(function(row, enc, next) {
        addMissingLicenseHeaders(row);
        this.push(row);
        next();
      })
    );

    const includedFiles = licensebookConfig.includedFiles;
    if (licensebookConfig.enabled) {
      b.pipeline.get('deps').push(
        through.obj(function(row, enc, next) {
          includedFiles.add(row.file);
          this.push(row);
          next();
        })
      );
    }

    var cacheData = {};

    for (var i = 0; i < included.length; i++) {
      // Non-linked path
      includedFiles.add(
        __dirname + '/../../../../../' + included[i] + '/index.js'
      );

      b.require(included[i]);
      cacheData[included[i]] = 'no idea ¯\\_(ツ)_/¯';
    }

    fs.readFile(cacheDest, 'utf8', function(err, previousCache) {
      if (!err && JSON.stringify(cacheData, null, '  ') === previousCache) {
        if (!licensebookConfig.enabled) {
          console.log('everything up to date');
          done();
          return;
        }
      }

      b.on('bundle:done', function(time) {
        console.log(dest + ' written in ' + time + 'ms');
      });

      b.on('error', function(err) {
        console.log('error', err);
      });

      function doBundle(cb) {
        b.bundle(function(err, buff) {
          if (err) {
            throw err;
          }
          require(dirname + '/node_modules/mkdirp')(
            dest.substr(0, dest.lastIndexOf('/'))
          )
            .then(() => {
              fs.writeFileSync(dest, buff.toString());
              fs.writeFileSync(
                cacheDest,
                JSON.stringify(cacheData, null, '  ')
              );
              done();
            })
            .catch(err => {
              if (err) {
                throw err;
              }
            });
        });
      }

      doBundle();
    });
  });
};
