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

/* global require: false */
'use strict';

var child_process = require('child_process');
var fs = require('fs');


/**
  This file is used to configure the [grunt](http://gruntjs.com/) tasks
  aimed to generate the web frontend of the Camunda Platform.
  @author Valentin Vago <valentin.vago@camunda.com>
  @author Sebastian Stamm  <sebastian.stamm@camunda.com>
 */

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);
  try {
    grunt.task.loadNpmTasks('grunt-contrib-watch');
  }
  catch (err) {
    grunt.log.errorlns(err.stack);
  }

  var commons = require('./index');

  function commonsConf() {
    var conf = commons.requirejs({ pathPrefix: '' });
    conf.baseUrl = '/';
    conf.packages.push({
      name: 'camunda-commons-ui/widgets',
      location: 'lib/widgets',
      main: 'index'
    });
    return conf;
    // return JSON.stringify(conf, null, 2);
  }

  var pkg = require('./package.json');

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;

  grunt.initConfig({
    commonsConf: commonsConf,

    protractor: {
      widgets: {   // Grunt requires at least one target to run so you can simply put 'all: {}' here too.
        options: {
          configFile: 'test/protractor.conf.js',
          seleniumAddress: 'http://localhost:4444/wd/hub',
        },
      },
    },

    connect: {
      options: {
        port: pkg.gruntConfig.connectPort,
        livereload: pkg.gruntConfig.livereloadPort
      },
      widgetTests: {
        options: {
          middleware: function (connect, options, middlewares) {
            middlewares.unshift(function (req, res, next) {
              if (req.url === '/test-conf.json') {
                res.setHeader('Content-Type', 'application/json');
                return res.end(JSON.stringify(commonsConf()));
              }
              next();
            });
            return middlewares;
          },
          base: [
            '.'
          ]
        }
      }
    },

    less: {
      options: {
        compress: true,
        sourceMap: true,
        sourceMapURL: './test-styles.css.map',
        sourceMapFilename: 'test-styles.css.map',
        paths: [
          'node_modules',
          'node_modules/bootstrap/less',
          'lib/widgets'
        ]
      },

      widgets: {
        files: {
          'test-styles.css': 'resources/less/test-styles.less'
        }
      }
    },

    watch: {
      options: {
        livereload: false,
      },

      styles: {
        files: [
          'lib/**/*.less',
          'resources/less/**/*.less'
        ],
        tasks: ['less']
      },

      scripts: {
        files: [
          'lib/**/*.js'
        ],
        tasks: ['newer:eslint']
      },

      served: {
        options: {
          livereload: pkg.gruntConfig.livereloadPort
        },
        files: [
          'kitchen-sink.html',
          '*.css',
          'lib/**/*.{html,js}'
        ],
        tasks: []
      }
    },

    browserify: {
      dist: {
        files: [{
          expand: true,     // Enable dynamic expansion.
          src: ['lib/widgets/**/test/*.src.js'], // Actual pattern(s) to match.
          ext: '.build.js',   // Dest filepaths will have this extension.
          extDot: 'first'   // Extensions in filenames begin after the first dot
        }]
      },

      watch: {
        files: [{
          expand: true,     // Enable dynamic expansion.
          src: ['lib/widgets/**/test/*.src.js'], // Actual pattern(s) to match.
          ext: '.build.js',   // Dest filepaths will have this extension.
          extDot: 'first'   // Extensions in filenames begin after the first dot
        }],
        options: {
          watch: true,
          transform: [
            [
              'babelify',
              {
                global: true,
                compact: false,
                presets: [
                  [
                    '@babel/preset-env',
                    {
                      targets:
                        'last 1 chrome version, last 1 firefox version, last 1 edge version',
                      forceAllTransforms: true
                    }
                  ]
                ]
              }
            ],
            'brfs']
        }
      }
    },

    eslint: {
      sources: {
        options: {
          ignorePattern: [
            'lib/**/*.build.js',
            'lib/**/*.page.js',
            'lib/**/*Spec.js'
          ]
        },
        src: [
         'lib/**/*.js'
        ]
      }
    }
  });

  grunt.registerTask('ensureSelenium', function() {
    // set correct webdriver version
    var done,
        path;

    if(fs.existsSync('node_modules/grunt-protractor-runner/node_modules/protractor/config.json')) {
      // npm 2
      path = 'node_modules/grunt-protractor-runner/node_modules/protractor/';
    } else if(fs.existsSync('node_modules/protractor/config.json')) {
      // npm 3+
      path = 'node_modules/protractor/';
    }

    fs.writeFileSync(path + 'config.json',
      '    {\n'+
      '      "webdriverVersions": {\n' +
      '        "selenium": "2.47.1",\n' +
      '        "chromedriver": "2.24",\n' +
      '        "iedriver": "2.47.0"\n' +
      '      }\n' +
      '    }'
    );

    // async task
    done = this.async();

    child_process.execFile('node', [__dirname + '/' + path + 'bin/webdriver-manager', '--chrome', 'update'], function(err) {
      done();
    });
  });

  grunt.registerTask('build', ['newer:eslint', 'less:widgets', 'browserify:watch']);

  grunt.registerTask('auto-build', ['build', 'connect:widgetTests', 'watch']);

  grunt.registerTask('default', ['build', 'ensureSelenium', 'connect:widgetTests', 'protractor:widgets']);

  grunt.registerTask('protractorTests', ['ensureSelenium', 'connect:widgetTests', 'protractor:widgets']);
};
