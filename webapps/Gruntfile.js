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
  require('time-grunt')(grunt);

  var verbose = grunt.option('verbose');
  var stack = grunt.option('stack');

  grunt.initConfig({
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
            args: [
              'clean',
              'install',
              'jetty:run',
              '-DskipTests',
              '-Pdevelop,livereload',
              //'-o'
            ]
          },
          autoBuild('camunda-bpm-sdk-js', verbose, stack),
          // autoBuild('camunda-commons-ui', verbose, stack),
          autoBuild('camunda-admin-ui', verbose, stack),
          autoBuild('camunda-cockpit-ui', verbose, stack),
          autoBuild('camunda-tasklist-ui', verbose, stack)
        ]
      }
    }
  });

  grunt.registerTask('setup', [
    'parallel:clone',
    'parallel:linkFrom',
    'parallel:bonerInstall',
    'parallel:linkTo'
  ]);

  grunt.registerTask('default', ['parallel:develop']);
};
