/* jshint node: true */
'use strict';
var path = require('path');

function autoBuild(project, verbose, stack) {
  var args = ['auto-build'];

  if (verbose) { args.unshift('--verbose'); }
  if (stack) { args.unshift('--stack'); }

  return {
    opts: {
      cwd: path.resolve(__dirname, '../'+ project)
    },
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

  var skipTests =   !grunt.option('run-tests');
  var mvnUpdate =   !grunt.option('offline');
  var verbose =     !!grunt.option('verbose');
  var stack =       !!grunt.option('stack');

  var webappArgs = [
    'clean',
    'install',
    'jetty:run',
    '-DskipTests',
    '-Pdevelop,livereload',
  ];

  if (!mvnUpdate) {
    webappArgs.push('-o');
  }

  grunt.log.subhead('Will start the platform with command');
  grunt.log.writeln('mvn '+ webappArgs.join(' '));
  grunt.log.writeln(skipTests ? 'Tests are skipped' : 'Tests will be performed on UI projects');

  var config = {
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
        ]
      },

      bonerInstall: {
        options: {
          stream: true
        },
        tasks: [
          bonerInstall('camunda-admin-ui'),
          bonerInstall('camunda-cockpit-ui'),
          bonerInstall('camunda-tasklist-ui'),
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
          autoBuild('camunda-admin-ui', verbose, stack),
          autoBuild('camunda-cockpit-ui', verbose, stack),
          autoBuild('camunda-tasklist-ui', verbose, stack),
          {
            opts: {},
            cmd: 'grunt',
            args: ['watch']
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
      }
    }
  };


  grunt.initConfig(config);


  grunt.registerTask('setup', [
    'parallel:clone',
    'parallel:linkFrom',
    'parallel:bonerInstall',
    'parallel:linkTo'
  ]);

  grunt.registerTask('default', ['parallel:develop']);
};
