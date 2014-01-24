var path = require('path');
var fs = require('fs');
var _ = require('underscore');

var rjsConf = require('./src/main/webapp/require-conf');

var livereloadPort = parseInt(process.env.LIVERELOAD_PORT || 8081, 10);


module.exports = function(grunt) {
  var packageJSON = grunt.file.readJSON('package.json');

  // Project configuration.
  grunt.initConfig({
    pkg: packageJSON,

    build: {
      production: {},
      development: {}
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
            return content
              .replace(/\/\* live-reload/, '/* live-reload */')
              .replace(/LIVERELOAD_PORT/g, livereloadPort);
          }
        }
      },

      // for now, copy as development, but leave the livereload comment
      production: {
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
          {
            expand: true,
            cwd: 'src/main/webapp/assets',
            src: [
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
          livereload: livereloadPort
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
    }


    // less: {
    //   options: {
    //     // paths: []
    //   },

    //   production: {
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

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-watch');
  // grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-requirejs');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-jsdoc');
  grunt.loadNpmTasks('grunt-bower-task');
  // grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-newer');

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

  // Aimed to hold more complex build processes
  grunt.registerMultiTask('build', 'Build the frontend assets', function() {
    var tasks = [
      'clean',
      'bower'
    ];

    if (this.target === 'production') {
      tasks = tasks.concat([
        // TODO: minification using ngr:
        // - Minifaction: https://app.camunda.com/jira/browse/CAM-1667
        // - Bug in ngDefine: https://app.camunda.com/jira/browse/CAM-1713
        'copy:assets',
        'copy:production'
      ]);
    }
    else {
      tasks = tasks.concat([
        'newer:copy:assets',
        'newer:copy:development'
        // 'copy:assets',
        // 'copy:development'
      ]);
    }

    // tasks.push('newer:less:'+ this.target);
    // tasks.push('less:'+ this.target);

    grunt.task.run(tasks);
  });

  grunt.registerTask('test', []);

  // Default task(s).
  grunt.registerTask('default', ['build:production']);
};
