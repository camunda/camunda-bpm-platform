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

var commonPackage = fs.readFileSync(__dirname + '/../../package.json', 'utf8');

var excluded = [
  'bpmn-font',
  'persistify',
  'mkdirp'
];

var included = [
  'angular',
  'moment',
  'camunda-bpm-sdk-js/lib/angularjs/index',
  'camunda-bpm-sdk-js',
  'q'
];


module.exports = function(grunt, dirname) {
  'use strict';
  grunt.registerMultiTask('ensureLibs', function() {

    var done = this.async();

    var packageJson = JSON.parse(commonPackage);

    var browserifyOptions = {
      transform: [
        ['envify',
          {
            NODE_ENV: 'development'
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
        ]],
      paths: [
        'node_modules',
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

    var b = require(dirname + '/node_modules/persistify')( browserifyOptions, persistifyOptions );

    var cacheData = {};

    for(var key in packageJson.dependencies) {
      if(excluded.indexOf(key) === -1) {
        b.require(key);
        cacheData[key] = packageJson.dependencies[key];
      }
    }
    for(var i = 0; i < included.length; i++) {
      // if(included[i].includes('camunda-bpm-sdk-js'))
      //   b.require('./' + included[i]);
      // else
        b.require(included[i]);
      cacheData[included[i]] = 'no idea ¯\\_(ツ)_/¯';
    }

    fs.readFile(cacheDest, 'utf8', function(err, previousCache) {
      if(!err && JSON.stringify(cacheData, null, '  ') === previousCache) {
        console.log('everything up to date');
        done();
        return;
      }

      b.on( 'bundle:done', function( time ) {
        console.log(dest + ' written in ' + time + 'ms');
      } );

      b.on( 'error', function( err ) {
        console.log( 'error', err );
      } );

      function doBundle(cb) {
        b.bundle( function( err, buff ) {
          if ( err ) {
            throw err;
          }
          require(dirname + '/node_modules/mkdirp')(dest.substr(0, dest.lastIndexOf('/')), function(err) {
            if(err) {
              throw err;
            }
            fs.writeFileSync( dest, buff.toString() );
            fs.writeFileSync( cacheDest, JSON.stringify(cacheData, null, '  '));
            done();
          });
        });
      }

      doBundle();
    });
  });
};
