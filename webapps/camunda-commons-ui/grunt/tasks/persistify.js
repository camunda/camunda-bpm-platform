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
var envify = require('envify/custom');

// Path is relative to the working dir the grunt task was started from
var packageJson = JSON.parse(fs.readFileSync('./package.json', 'utf8'));

module.exports = function(grunt, dirname, licensebookConfig) {
  'use strict';
  grunt.registerMultiTask('persistify', function() {

    var done = this.async();

    var externalModules = JSON.parse(fs.readFileSync(__dirname + '/../../cache/deps.json', 'utf8'));

    var firstRun = true;
    var dest = this.data.dest;
    var opts = this.data.options;

    this.data.options.neverCache = [
      /\.html$/,
      /\.json$/
    ];

    this.data.options.recreate = !process.env.FAST_BUILD;

    // backwards compatibility with grunt-browserify
    if(this.data.options.transform) {
      this.data.options.browserifyOptions.transform = this.data.options.transform;
    }

    var b = require(dirname + '/node_modules/persistify')( this.data.options.browserifyOptions, this.data.options, { "ignore-watch": false } );

    b.transform('brfs', { global: true });
    b.transform(envify({
      CAMUNDA_VERSION: packageJson.version
    }))


    for(var key in externalModules) {
      b.external(key);
    }

    b.add( this.data.src );

    if(licensebookConfig.enabled) {
      b.pipeline.get("deps").push(through.obj(function(row, enc, next) {
        licensebookConfig.includedFiles.add(row.file);
        this.push(row);
        next();
      }));
    }

    b.on( 'bundle:done', function( time ) {
      console.log(dest + ' written in ' + time + 'ms');
    } );

    b.on( 'error', function( err ) {
      console.log( 'error', err );
    } );

    function bundleComplete(err, buff) {
      if ( err ) {
        throw err;
      }
      require(dirname + '/node_modules/mkdirp')(dest.substr(0, dest.lastIndexOf('/')), function(err) {
        if(err) {
          throw err;
        }
        fs.writeFileSync( dest, buff.toString() );
        if(firstRun) {
          firstRun = false;
          done();
        }
      });
    }

    function doBundle() {
      b.bundle( function( err, buff ) {
        if (opts.postBundleCB) {
          opts.postBundleCB(err, buff, bundleComplete);
        }
        else {
          bundleComplete(err, buff);
        }
      });
    }

    b.on( 'update', function() {
      console.log('change detected, updating ' + dest);
      doBundle();
    });

    doBundle();

  });
};
