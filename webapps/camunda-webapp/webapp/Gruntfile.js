var path = require('path');
var fs = require('fs');
var _ = require('underscore');

var rjsConf = require('./src/main/webapp/require-conf');

var commentLineExp = /^[\s]*<!-- (\/|#) (CE|EE)/;

function distFileProcessing(content, srcpath) {
  // removes the template comments
  content = content
            .split('\n').filter(function(line) {
              // console.info(line.slice(0, 10), !commentLineExp.test(line));
              return !commentLineExp.test(line);
            }).join('\n');

  return content;
}


function developmentFileProcessing(content, srcpath) {
  // Unfortunately, this might (in some cases) make angular complaining
  // about template having no single root element
  // (when the "replace" option is set to "true").

  // if (/\.html$/.test(srcpath)) {
  //   content = '<!-- # CE - auto-comment - '+ srcpath +' -->\n'+
  //             content +
  //             '\n<!-- / CE - auto-comment - '+ srcpath +' -->';
  // }

  if (/require-conf.js$/.test(srcpath)) {
    content = content
              .replace(/\/\* live-reload/, '/* live-reload */')
              .replace(/LIVERELOAD_PORT/g, "<%= app.liveReloadPort %>");
  }
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
      liveReloadPort: parseInt(process.env.LIVERELOAD_PORT || 8081, 10)
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
              '{app,plugin,develop,common}/{,**/}*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: function(content, srcpath) {

            var liveReloadPort = grunt.config('app.liveReloadPort');

            return content
              .replace(/\/\* live-reload/, '/* live-reload */')
              .replace(/LIVERELOAD_PORT/g, liveReloadPort);
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
              '{app,plugin,develop,common}/{,**/}*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: distFileProcessing
        }
      },

      // for now, copy as development, but leave the livereload comment
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
              '{app,plugin,develop,common}/{,**/}*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ]
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
      },

      // TODO: remove that when using less
      css: {
        files: [
          {
            expand: true,
            cwd: 'src/main/webapp/assets',
            src: [
              'css/**/*'
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
          // 'jshint:scripts',
          'newer:copy:development'
          // 'copy:development'
        ]
      },

      // watch for source script and test changes
      // QUESTION:
      // Does that entry make sense?
      // We can use `karma:unit` and `karma:e2e` instead of watching
      // tests: {
      //   files: [
      //     'src/main/webapp/require-conf.js',
      //     'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}',
      //     'src/test/js/{config,e2e,test,unit}/{,**/}*.js'
      //   ],
      //   tasks: [
      //     // 'jshint:test',
      //     // we use the CI versions (who are runned only once)
      //     // 'karma:testOnce',
      //     'karma:unitOnce',
      //     'karma:e2eOnce'
      //   ]
      // },

      // TODO: add that when using less
      // styles: {
      //   files: [
      //     'src/main/webapp/styles/{**/,}*.less'
      //   ],
      //   tasks: [
      //     'newer:less:development'
      //   ]
      // },

      // TODO: remove that when using less
      css: {
        files: [
          'src/main/webapp/assets/css/**/*.css'
        ],
        tasks: [
          'newer:copy:css'
          // 'copy:css'
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

    // jshint: {
    //   options: {
    //     browser: true,
    //     globals: {
    //       angular: true,
    //       jQuery: true
    //     }
    //   },
    //   test: {
    //     files: {
    //       src: [
    //         'test/js/{config,e2e,unit}/{,**/}*.js'
    //       ]
    //     }
    //   },
    //   scripts: {
    //     files: {
    //       src: [
    //         'Gruntfile.js',
    //         'src/main/webapp/{app,assets,develop,plugin}/{,**/}*.js'
    //       ]
    //     }
    //   }
    // },

    // karma: {
    //   options: {
    //     browsers: ['Chrome', 'Firefox']//, 'IE']
    //   },

    //   // to test the testing environment
    //   test: {
    //     configFile: 'src/test/js/config/karma.test.js'
    //   },

    //   unit: {
    //     configFile: 'src/test/js/config/karma.unit.js'
    //   },
    //   e2e: {
    //     configFile: 'src/test/js/config/karma.e2e.js'
    //   },

    //   //continuous integration mode: run tests once in PhantomJS browser.
    //   unitOnce: {
    //     singleRun: true,
    //     autoWatch: false,
    //     configFile: 'src/test/js/config/karma.unit.js',
    //     browsers: ['PhantomJS']
    //   },
    //   e2eOnce: {
    //     singleRun: true,
    //     autoWatch: false,
    //     configFile: 'src/test/js/config/karma.e2e.js',
    //     browsers: ['PhantomJS']
    //   }
    // },

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
          configure: './jsdoc-conf.json',
          destination: 'doc'
        }
      }
    },

    bower: {
      install: {}
    },

    requirejs: {
    // ngr: {
      // see https://github.com/jrburke/r.js/blob/master/build/example.build.js
      options: {
        baseUrl: 'src/main/webapp',

        dir: 'target/webapp',

        // Inlines the text for any text! dependencies, to avoid the separate
        // async XMLHttpRequest calls to load those dependencies.
        inlineText: true,

        optimize: 'none',

        paths: rjsConf.paths,
        shim: rjsConf.shim,

        // CommonJS packages support
        // http://requirejs.org/docs/api.html#packages
        packages: rjsConf.packages,

        //
        optimizeCss: 'none'
      },

      app: {
        modules: [{
          name: 'src/main/webapp/app/app',
          out: 'app/app.js',
          override: {},
          exclude: [
            'ngDefine'
          ]
        }]
      },

      admin: {
        modules: [{
          name: 'app/admin/admin',
          out: 'app/admin.min.js',
          override: {},
          exclude: []
        }]
      },

      cockpit: {
        modules: [
          {
            name: 'app/cockpit/cockpit',
            out: 'app/cockpit.min.js',
            override: {},
            exclude: []
          }
        ]
      },

      tasklist: {
        modules: [{
          name: 'src/main/webapp/app/tasklist/tasklist',
          out: 'app/tasklist.js',
          override: {},
          exclude: []
        }]
      }
    },

    open: {
      server: {
        url: 'http://localhost:<%= app.port %>/camunda'
      }
    },

    // less: {
    //   options: {
    //     // paths: []
    //   },

    //   dist: {
    //     options: {
    //       cleancss: true
    //     },
    //     files: {
    //       'target/webapp/assets/css/common.css': 'src/main/webapp/styles/common.less',
    //       'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/styles/cockpit/loader.less',
    //       'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/styles/admin/loader.less',
    //       'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/styles/tasklist/loader.less'
    //     }
    //   },

    //   development: {
    //     files: {
    //       'target/webapp/assets/css/common.css': 'src/main/webapp/styles/common.less',
    //       'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/styles/cockpit/loader.less',
    //       'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/styles/admin/loader.less',
    //       'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/styles/tasklist/loader.less'
    //     }
    //   }
    // }
  });

  // custom task for ngDefine minification
  grunt.registerMultiTask('ngr', 'Minifies the angular related scripts', function() {
    var done = this.async();
    var ngr = require('requirejs-angular-define/src/ngr');

    var setup = _.extend({}, this.options(), this.data);
    // console.info('ngr options', setup);

    ngr.optimize(setup, function() {
      console.info('optimized', arguments.length);
      done();
    }, function(e) {
      console.log('Error during minify: ', e);
      done(new Error('With failures: ' + e));
    });
  });

  // automatically (re-)build web assets
  grunt.registerTask('auto-build', 'Continuously (re-)build front-end assets', function (target) {

    if (target === 'dist') {
      throw new Error('dist target not yet supported');
    }

    grunt.task.run([
      'build',
      'open',
      'watch'
    ]);
  });

  // Aimed to hold more complex build processes
  grunt.registerTask('build', 'Build the frontend assets', function(target) {
    var defaultTasks = [
      'clean',
      'bower'
    ];

    if (target === 'dist') {
      // TODO: minification using ngr:
      // - Minifaction: https://app.camunda.com/jira/browse/CAM-1667
      // - Bug in ngDefine: https://app.camunda.com/jira/browse/CAM-1713

      return grunt.task.run(defaultTasks.concat([
        'copy:assets',
        'copy:dist'
      ]));
    }

    // tasks.push('newer:less:'+ this.target);
    // tasks.push('less:'+ this.target);

    return grunt.task.run(defaultTasks.concat([
      'newer:copy:assets',
      'newer:copy:development'
      // 'copy:assets',
      // 'copy:development'
    ]));
  });

  grunt.registerTask('test', []);

  // Default task(s).
  grunt.registerTask('default', ['build:dist']);
};
