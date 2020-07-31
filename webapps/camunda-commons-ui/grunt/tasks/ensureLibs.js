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

var included = [
  'angular',
  'moment',
  'camunda-bpm-sdk-js/lib/angularjs/index',
  'camunda-bpm-sdk-js',
  'q',
  'angular-animate',
  'angular-cookies',
  'angular-data-depend',
  'angular-loader',
  'angular-mocks',
  'angular-moment',
  'angular-resource',
  'angular-route',
  'angular-sanitize',
  'angular-scenario',
  'angular-touch',
  'angular-translate',
  'bootstrap',
  'bpmn-js',
  'clipboard',
  'cmmn-js',
  'dmn-js',
  'jquery',
  'lodash',

  // Okay, this looks bad. But hear me out:
  // We create our own DMN editor using DMN-JS
  // DMN-JS only exports a viewer on the default entry, so we have to require the modules directly
  // As we don't want to ship dmn-js multiple times, we need to bundle all the required libs here as well
  // let's hope we can refactor this when we introduce DMN 1.3
  // The culprit is over here:
  // camunda-bpm-webapp/camunda-commons-ui/lib/widgets/dmn-viewer/lib/navigatedViewer.js
  'dmn-js-shared/lib/base/Manager',
  'dmn-js-drd/lib/NavigatedViewer',
  'dmn-js-decision-table/lib/Viewer',
  'dmn-js-literal-expression/lib/Viewer',
  'dmn-js-shared/lib/util/ModelUtil',
  'dmn-js-shared/lib/util/DiUtil',
  'dmn-js/lib/Modeler'
];


module.exports = function(grunt, dirname, licensebookConfig) {
  grunt.registerMultiTask('ensureLibs', function() {

    var done = this.async();

    var browserifyOptions = {
      transform: [
        ['envify',
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
        ]],
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

    var b = require(dirname + '/node_modules/persistify')( browserifyOptions, persistifyOptions );


    const includedFiles = licensebookConfig.includedFiles;
    if(licensebookConfig.enabled) {
      b.pipeline.get("deps").push(through.obj(function(row, enc, next) {
        includedFiles.add(row.file);
        this.push(row);
        next();
      }));
    }

    var cacheData = {};

    for(var i = 0; i < included.length; i++) {
      // Non-linked path
      includedFiles.add(__dirname + '/../../../../../node_modules/' + included[i] + '/index.js');

      b.require(included[i]);
      cacheData[included[i]] = 'no idea ¯\\_(ツ)_/¯';
    }

    fs.readFile(cacheDest, 'utf8', function(err, previousCache) {
      if(!err && JSON.stringify(cacheData, null, '  ') === previousCache) {
        if (!licensebookConfig.enabled) {   
          console.log('everything up to date');
          done();
          return;
        }
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
