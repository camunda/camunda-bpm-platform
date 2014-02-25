/* global process: false, require: false, module: false, __dirname: false */
'use strict';

/**
  This file is used to configure the [grunt](http://gruntjs.com/) tasks
  aimed to generate the web frontend of the camunda BPM platform.
  @author Valentin Vago <valentin.vago@camunda.com>
  @author Nico Rehwaldt  <nico.rehwaldt@camunda.com>
 */

var spawn = require('child_process').spawn;
var path = require('path');
var fs = require('fs');
var _ = require('underscore');

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;
var seleniumJarNameExp = /selenium-server-standalone/;


function distFileProcessing(content, srcpath) {
  // removes the template comments
  content = content
            .split('\n').filter(function(line) {
              return !commentLineExp.test(line);
            }).join('\n');

  var date = new Date();
  var cacheBuster = [date.getFullYear(), date.getMonth(), date.getDate()].join('-');
  content = content
            .replace(/\/\* cache-busting /, '/* cache-busting */')
            .replace(/CACHE_BUSTER/g, requireConfExp.test(srcpath) ? '\''+ cacheBuster +'\'' : cacheBuster);

  return content;
}

function standaloneSeleniumJar() {
  if (!fs.existsSync('selenium')) {
    return false;
  }
  var filename = _.find(fs.readdirSync('selenium'), function(filename) {
    return seleniumJarNameExp.test(filename);
  });
  return path.join(__dirname, 'selenium', filename);
}

module.exports = function(grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  var packageJSON = grunt.file.readJSON('package.json');

  // Project configuration.
  grunt.initConfig({
    pkg: packageJSON,

    app: {
      port: parseInt(process.env.APP_PORT || 8080, 10),
      liveReloadPort: parseInt(process.env.LIVERELOAD_PORT || 8081, 10),
      standaloneSeleniumJar: standaloneSeleniumJar
    },

    clean: {
      target: [
        'target/webapp'
      ],
      assets: [
        'target/webapp/assets'
      ]
    },

    copy: {
      development: {
        files: [
          {
            expand: true,
            cwd: 'src/main/webapp/WEB-INF',
            src: ['*'],
            dest: 'target/webapp/WEB-INF'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              'require-conf.js',
              'index.html'
            ],
            dest: 'target/webapp/'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              '{app,plugin,develop,common}/**/*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: function(content, srcpath) {

            var liveReloadPort = grunt.config('app.liveReloadPort');

            if (requireConfExp.test(srcpath)) {
              content = content
                        .replace(/\/\* live-reload/, '/* live-reload */')
                        .replace(/LIVERELOAD_PORT/g, liveReloadPort);
            }

            content = content
                      .replace(/\/\* cache-busting/, '/* cache-busting */')
                      .replace(/CACHE_BUSTER/g, (new Date()).getTime());
            return content;
          }
        }
      },
      dist: {
        files: [
          {
            expand: true,
            cwd: 'src/main/webapp/WEB-INF',
            src: ['*'],
            dest: 'target/webapp/WEB-INF'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              'require-conf.js',
              'index.html'
            ],
            dest: 'target/webapp/'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              '{app,plugin,develop,common}/**/*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: distFileProcessing
        }
      },

      assets: {
        files: [
          // requirejs
          {
            src: 'src/main/webapp/assets/vendor/requirejs/index.js',
            dest: 'target/webapp/assets/vendor/requirejs/require.js'
          },
          // others
          {
            expand: true,
            cwd: 'src/main/webapp/assets',
            src: [
              '!vendor/requirejs/**/*',
              'css/**/*',
              'img/**/*',
              'vendor/**/*.{js,css,jpg,png,gif,html,eot,ttf,svg,woff}'
            ],
            dest: 'target/webapp/assets'
          }
        ]
      }
    },

    watch: {
      options: {
        livereload: false
      },

      // watch for source script changes
      scripts: {
        files: [
          'src/main/webapp/require-conf.js',
          'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}'
        ],
        tasks: [
          'newer:jshint:scripts',
          'newer:copy:development'
        ]
      },

      unitTests: {
        files: [
          'src/main/webapp/require-conf.js',
          'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}',
          'src/test/js/{config,test,unit}/**/*.js'
        ],
        tasks: [
          'newer:jshint:unitTest',
          'karma:test',
          'karma:unit'
        ]
      },

      e2eTests: {
        // runs only when the tests are modified
        files: [
          // 'src/main/webapp/require-conf.js',
          // 'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}',
          './../../../qa/integration-tests-webapps/src/test/javascript/e2e/**/*.js'
        ],
        tasks: [
          'newer:jshint:e2eTest',
          'test:e2e'
        ]
      },

      styles: {
        files: [
          'src/main/webapp/assets/styles/**/*.less'
        ],
        tasks: [
          'less:development'
        ]
      },

      servedAssets: {
        options: {
          livereload: '<%= app.liveReloadPort %>'
        },
        files: [
          'target/webapp/assets/{css,img}/**/*.{css,jpg,png,html}',
          'target/webapp/{app,plugin,develop}/**/*.{css,js,html,jpg,png}'
        ],
        tasks: []
      }
    },

    jshint: {
      options: {
        browser: true,
        globals: {
          angular:  false,
          jQuery:   false,
          ngDefine: false
        }
      },

      unitTest: {
        files: {
          src: [
            // 'src/test/js/{config,test,unit}/**/*.js'
          ]
        }
      },

      e2eTest: {
        files: {
          src: [
            // 'src/test/js/e2e/**/*.js'
          ]
        }
      },

      scripts: {
        files: {
          src: [
            'Gruntfile.js',
            // 'src/main/webapp/{app,assets,develop,plugin}/**/*.js'
          ]
        }
      }
    },

    karma: {
      // to test the testing environment
      test: {
        configFile: 'src/test/js/config/karma.test.js'
      },

      unit: {
        configFile: 'src/test/js/config/karma.unit.js'
      }
    },

    // https://www.npmjs.org/package/grunt-protractor-runner
    protractor: {
      options: {
        singleRun: true,
        // config like in *.conf.js
        args: {
          // use a function(!!!) to determine the path of selenium standalone web-driver
          seleniumServerJar: '<%= app.standaloneSeleniumJar() %>',
          specs: [
            './../../../qa/integration-tests-webapps/src/test/javascript/e2e/**/*.js'
          ],

          capabilities: {
            browserName: 'chrome'
          },

          // // If you would like to run more than one instance of webdriver on the same
          // // tests, use multiCapabilities, which takes an array of capabilities.
          // // If this is specified, capabilities will be ignored.
          // multiCapabilities: [
          //   {
          //     browserName: 'chrome'
          //   },
          //   {
          //     browserName: 'phantomjs'
          //   }
          // ],

          baseUrl: 'http://localhost:8080',

          // ----- The test framework -----
          //
          // Jasmine is fully supported as a test and assertion framework.
          // Mocha has limited beta support. You will need to include your own
          // assertion framework if working with mocha.
          framework: 'jasmine',

          // ----- Options to be passed to minijasminenode -----
          //
          // Options to be passed to Jasmine-node.
          // See the full list at https://github.com/juliemr/minijasminenode
          jasmineNodeOpts: {
            defaultTimeoutInterval: 15000, // Default time to wait in ms before a test fails.
            showColors: true, // Use colors in the command line report.
            includeStackTrace: true, // If true, include stack traces in failures.
          }
        }
      },

      admin: {
        options:{
          args: {
            baseUrl: 'http://localhost:8080',
            specs: [
              './../../../qa/integration-tests-webapps/src/test/javascript/e2e/admin/**/*.js'
            ]
          }
        }
      },
      cockpit: {
        options:{
          args: {
            baseUrl: 'http://localhost:8080',
            specs: [
              './../../../qa/integration-tests-webapps/src/test/javascript/e2e/cockpit/**/*.js'
            ]
          }
        }
      },
      tasklist: {
        options:{
          args: {
            baseUrl: 'http://localhost:8080',
            specs: [
              './../../../qa/integration-tests-webapps/src/test/javascript/e2e/tasklist/**/*.js'
            ]
          }
        }
      }
    },

    jsdoc : {
      dist : {
        src: [
          'README.md',
          'src/main/webapp/require-conf.js',
          'src/main/webapp/app',
          'src/main/webapp/develop',
          'src/main/webapp/plugin'
        ],

        options: {
          // grunt-jsdoc has a big problem... some kind of double-parsing...
          // using the `jsdoc -d doc -r -c jsdoc-conf.json` command works fine
          // configure: './jsdoc-conf.json',
          destination: 'doc'
        }
      }
    },

    bower: {
      install: {}
    },

    less: {
      options: {
        // paths: []
      },

      dist: {
        options: {
          cleancss: true
        },
        files: {
          'target/webapp/assets/css/common.css': 'src/main/webapp/assets/styles/common.less',
          'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/assets/styles/cockpit/loader.less',
          'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/assets/styles/admin/loader.less',
          'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/assets/styles/tasklist/loader.less'
        }
      },

      development: {
        files: {
          'target/webapp/assets/css/common.css': 'src/main/webapp/assets/styles/common.less',
          'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/assets/styles/cockpit/loader.less',
          'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/assets/styles/admin/loader.less',
          'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/assets/styles/tasklist/loader.less'
        }
      }
    },

    open: {
      server: {
        url: 'http://localhost:<%= app.port %>/camunda'
      }
    }
  });

  grunt.registerTask('selenium-install', 'Automate the selenium webdriver installation', function() {
    var done = this.async();
    var stdout = '';
    var stderr = '';

    var managerPath = './node_modules/grunt-protractor-runner/node_modules/protractor/bin/webdriver-manager';
    var args = [
      'update',
      '--out_dir',
      __dirname +'/selenium'
    ];

    grunt.log.writeln('selenium-install runs: '+ managerPath +' '+ args.join(' '));

    var install = spawn(managerPath, args);

    install.stdout.on('data', function(data) { stdout += data; });
    install.stderr.on('data', function(data) { stderr += data; });

    install.on('exit', function (code) {
      if (code) {
        return done(new Error('selenium-install exit with code: '+ code));
      }

      grunt.log.writeln('selenium standalone server installed');
      done();
    });
  });

  // automatically (re-)build web assets
  grunt.registerTask('auto-build', 'Continuously (re-)build front-end assets', function (target) {
    if (target === 'dist') {
      throw new Error('dist target not yet supported');
    }

    grunt.task.run([
      'build:development',
      'open',
      'watch'
    ]);
  });

  grunt.registerTask('test', 'Run the tests (by default: karma:unit)', function(target, set) {
    var tasks = [];

    switch (target) {
      // test the testing environment
      case 'test':
        tasks.push('karma:test');
        break;

      // should use protractor
      case 'e2e':
        tasks.push('selenium-install');
        tasks.push('protractor'+ (set ? ':'+ set : ''));
        break;

      // unit testing by default
      default:
        tasks.push('karma:unit');
    }


    return grunt.task.run(tasks);
  });

  // Aimed to hold more complex build processes
  grunt.registerTask('build', 'Build the frontend assets', function(target) {
    var tasks = [
      'clean',
      'bower'
    ];

    if (target === 'dist') {
      // TODO: minification using ngr:
      // - Minifaction: https://app.camunda.com/jira/browse/CAM-1667
      // - Bug in ngDefine: https://app.camunda.com/jira/browse/CAM-1713

      tasks = tasks.concat([
        'copy:assets',
        'copy:dist'
      ]);
    }


    tasks = tasks.concat([
      'less:'+ target,
      'newer:copy:assets',
      'newer:copy:'+ target
    ]);

    return grunt.task.run(tasks);
  });

  // Default task(s).
  grunt.registerTask('default', ['build:dist']);
};
