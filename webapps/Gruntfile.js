/* global process: false, require: false, module: false, __dirname: false */
'use strict';

/**
  This file is used to configure the [grunt](http://gruntjs.com/) tasks
  aimed to generate the web frontend of the camunda BPM platform.
  @author Valentin Vago <valentin.vago@camunda.com>
  @author Nico Rehwaldt  <nico.rehwaldt@camunda.com>
 */

var fs = require('fs');
var _ = require('underscore');

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var requireConfExp =  /require-conf.js$/;

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
    },

    clean: {
      target: [
        '<%= grunt.option("target") %>/',
        'doc/'
      ]
    },

    mkdir: {
      target: {
        options: {
          create: ['<%= grunt.option("target") %>/']
        },
      },
    },

    copy: {
      development: {
        files: [
          {
            expand: true,
            cwd: 'app/',
            src: ['**'],
            dest: grunt.option("target"),
          },
          {
            expand: true,
            cwd: 'assets/',
            src: [
              'fonts/**/*.{css,eot,svg,ttf,woff}'
            ],
            dest: grunt.option("target") + '/assets'
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
            cwd: 'app/',
            src: ['**'],
            dest: grunt.option("target"),
          },
        ],
        options: {
          process: distFileProcessing
        }
      },

      assets: {
        files: [
          {
            expand: true,
            cwd: 'bower_components/',
            src: '**',
            dest: grunt.option("target") + '/assets/vendor/'
          },
          {
            expand: true,
            cwd: 'assets/',
            src: [ 'img/**' ],
            dest: grunt.option("target") + '/assets'
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
          'app/**/*.{js,html}'
        ],
        tasks: [
          // 'newer:jshint:scripts',
          'newer:copy:development'
        ]
      },

      styles: {
        files: [
          'assets/**/*.less'
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
          grunt.option("target") + '/**/*.{css,js,html,jpg,png}'
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
            'test/{config,test,unit}/**/*.js'
          ]
        }
      },

      e2eTest: {
        files: {
          src: [
            'test/e2e/**/*.js'
          ]
        }
      },

      scripts: {
        files: {
          src: [
            'Gruntfile.js',
            'app/**/*.js'
          ]
        }
      }
    },

    karma: {
      // to test the testing environment
      test: {
        configFile: 'test/config/karma.test.js'
      },

      unit: {
        configFile: 'test/config/karma.unit.js'
      }
    },

    jsdoc : {
      dist : {
        src: [
          'README.md',
          'app/',
          'bower_components/camunda-commons-ui/lib/'
        ],

        options: {
          // grunt-jsdoc has a big problem... some kind of double-parsing...
          // using the `jsdoc -d doc -r -c jsdoc-conf.json` command works fine
          // configure: './jsdoc-conf.json',
          destination: 'doc'
        }
      }
    },

    less: {
      options: {
        // paths: []
      },

      dist: {
        options: {
          compress: true
        },
        files: {
          '<%= grunt.option("target") %>/assets/css/loader.css': 'assets/styles/loader.less',
        }
      },

      development: {
        files: {
          '<%= grunt.option("target") %>/assets/css/loader.css': 'assets/styles/loader.less',
        }
      }
    }

  });


  // automatically (re-)build web assets
  grunt.registerTask('auto-build', 'Continuously (re-)build front-end assets', function (target) {
    if (target === 'dist') {
      throw new Error('dist target not yet supported');
    }

    grunt.task.run([
      'build:development',
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

      // unit testing by default
      default:
        // tasks.push('karma:unit');
    }


    return grunt.task.run(tasks);
  });

  // Aimed to hold more complex build processes
  grunt.registerTask('build', 'Build the frontend assets', function(target) {
    target = target || 'dist';

    var tasks = [
      'clean:target',
      'mkdir:target',
      'copy:assets',
      'copy:dist',
      'less:'+ target,
      'newer:copy:assets',
      'newer:copy:'+ target
    ];

    return grunt.task.run(tasks);
  });

  // Default task(s).
  grunt.registerTask('default', ['build:dist']);

};
