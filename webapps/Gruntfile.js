/* jshint node: true */
'use strict';
var path = require('path');
var http = require('http');
var spawn = require('child_process').spawn;

function testOnline(done) {
  http.get('http://localhost:8080/camunda/app/tasklist/default', function(res) {
    done(res.statusCode !== 200 ? new Error('The status code is not 200') : null);
  })
  .on('error', done);
}

function autoBuild(project, verbose, stack) {
  var args = ['auto-build'];

  if (verbose) { args.unshift('--verbose'); }
  if (stack) { args.unshift('--stack'); }

  return {
    opts: {
      cwd: path.resolve(__dirname, '../'+ project)
    },
    // cmd: './node_modules/grunt-cli/bin/grunt',
    cmd: 'grunt',
    args: args
  };
}

function clone(project) {
  return {
    opts: {
      cwd: __dirname +'/../'
    },
    cmd: 'git',
    args: [
      'clone',
      'git@github.com:camunda/'+ project +'.git',
    ]
  };
}

function linkFrom(project) {
  return {
    opts: {
      cwd: __dirname +'/../'+ project
    },
    cmd: 'npm',
    args: [
      'link'
    ]
  };
}

function bonerInstall(project) {
  return {
    opts: {
      cwd: __dirname +'/../'+ project
    },
    cmd: './node_modules/bower/bin/bower',
    args: [
      'install'
    ]
  };
}

function linkTo(project) {
  return {
    opts: {},
    cmd: 'npm',
    args: [
      'link',
      project
    ]
  };
}


module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);

  var verbose = grunt.option('verbose');
  var stack = grunt.option('stack');
  var webappArgs = [
    'clean',
    'install',
    'jetty:run',
    '-DskipTests',
    '-Pdevelop,livereload',
  ];

  if (!grunt.option('update')) {
    webappArgs.push('-o');
  }

  grunt.log.subhead('Will start the platform with command');
  grunt.log.writeln('mvn '+ webappArgs.join(' '));

  grunt.initConfig({
    copy: {
      develop: {
        files: [
          {
            expand: true,
            cwd: 'webapp/src/main/webapp/',
            src: ['**/*.html'],
            dest: 'webapp/target/camunda-webapp/'
          }
        ]
      }
    },

    watch: {
      develop: {
        files: 'webapp/src/main/webapp/**/*.html',
        tasks: [
          'newer:copy:develop'
        ]
      },

      targetAdmin: {
        options: {
          debounceDelay: 1000,
        },
        files: [
          'webapp/src/test/js/e2e/admin/**/*.js',
          // 'webapp/target/camunda-webapp/app/admin/**/*.{js,html}',
          // '!webapp/target/camunda-webapp/app/admin/assets/**'
          '../camunda-admin-ui/dist/app/admin/**/*.{js,html}',
          '!../camunda-admin-ui/dist/app/admin/assets/**'
        ],
        tasks: [
          'test:admin'
        ]
      },
      targetCockpit: {
        options: {
          debounceDelay: 1000,
        },
        files: [
          'webapp/src/test/js/e2e/cockpit/**/*.js',
          // 'webapp/target/camunda-webapp/app/cockpit/**/*.{js,html}',
          // '!webapp/target/camunda-webapp/app/cockpit/assets/**'
          '../camunda-cockpit-ui/dist/app/cockpit/**/*.{js,html}',
          '!../camunda-cockpit-ui/dist/app/cockpit/assets/**'
        ],
        tasks: [
          'test:cockpit'
        ]
      },
      targetTasklist: {
        options: {
          debounceDelay: 1000,
        },
        files: [
          'webapp/src/test/js/e2e/tasklist/**/*.js',
          // 'webapp/target/camunda-webapp/app/tasklist/**/*.{js,html}',
          // '!webapp/target/camunda-webapp/app/tasklist/vendor/**'
          '../camunda-tasklist-ui/dist/app/tasklist/**/*.{js,html}',
          '!../camunda-tasklist-ui/dist/app/tasklist/vendor/**'
        ],
        tasks: [
          'test:tasklist'
        ]
      }
    },

    parallel: {
      clone: {
        options: {
          stream: true
        },
        tasks: [
          clone('camunda-bpm-sdk-js'),
          clone('camunda-commons-ui'),
          clone('camunda-admin-ui'),
          clone('camunda-cockpit-ui'),
          clone('camunda-tasklist-ui'),
          clone('camunda-cockpit-plugin-base')
        ]
      },

      linkFrom: {
        options: {
          stream: true
        },
        tasks: [
          linkFrom('camunda-bpm-sdk-js'),
          linkFrom('camunda-commons-ui'),
          linkFrom('camunda-admin-ui'),
          linkFrom('camunda-cockpit-ui'),
          linkFrom('camunda-tasklist-ui'),
          // linkFrom('camunda-cockpit-plugin-base')
        ]
      },

      bonerInstall: {
        options: {
          stream: true
        },
        tasks: [
          // bonerInstall('camunda-bpm-sdk-js'),
          // bonerInstall('camunda-commons-ui'),
          bonerInstall('camunda-admin-ui'),
          bonerInstall('camunda-cockpit-ui'),
          bonerInstall('camunda-tasklist-ui'),
          // bonerInstall('camunda-cockpit-plugin-base')
        ]
      },

      linkTo: {
        options: {
          stream: true
        },
        tasks: [
          linkTo('camunda-bpm-sdk-js'),
          linkTo('camunda-commons-ui'),
          linkTo('camunda-admin-ui'),
          linkTo('camunda-cockpit-ui'),
          linkTo('camunda-tasklist-ui'),
          // linkTo('camunda-cockpit-plugin-base')
        ]
      },

      develop: {
        options: {
          stream: true
        },
        tasks: [
          {
            opts: {
              cwd: path.resolve(__dirname, './webapp')
            },
            cmd: 'mvn',
            args: webappArgs
          },
          autoBuild('camunda-bpm-sdk-js', verbose, stack),
          // autoBuild('camunda-commons-ui', verbose, stack),
          autoBuild('camunda-admin-ui', verbose, stack),
          autoBuild('camunda-cockpit-ui', verbose, stack),
          autoBuild('camunda-tasklist-ui', verbose, stack),
          {
            cmd: 'webdriver-manager',
            args: ['start']
          },
          {
            opts: {},
            cmd: 'grunt',
            args: ['watch']
          }
        ]
      }
    }
  });

  grunt.registerTask('test', function(target) {
    var done = this.async();

    grunt.log.subhead('Testing '+ target +' UI');
    var stdout = '';
    var stderr = '';
    var args = [
      '--specs',
      'webapp/src/test/js/e2e/'+ target +'/spec/**/*.js',
      'webapp/src/test/js/e2e/develop.conf.js'
    ];

    testOnline(function(err) {
      if (err) {
        grunt.log.writeln(err.message);
        return;
      }

      var testRun = spawn('protractor', args);

      testRun.stdout.on('data', function (data) {
        console.info(data.toString());
        stdout += data;
      });

      testRun.stderr.on('data', function (data) {
        console.info(data.toString());
        stderr += data;
      });

      testRun.on('close', function(code) {
        grunt.file.write('test.'+ target +'.out.log', stdout);
        grunt.file.write('test.'+ target +'.err.log', stderr);
        grunt.log.writeln('protractor '+ args.join(' ') +' exited with code: '+ code);
        done();
      });
    });
  });

  grunt.registerTask('setup', [
    'parallel:clone',
    'parallel:linkFrom',
    'parallel:bonerInstall',
    'parallel:linkTo'
  ]);

  grunt.registerTask('default', ['parallel:develop']);
};
