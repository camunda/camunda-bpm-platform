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


module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  var verbose = grunt.option('verbose');
  var stack = grunt.option('stack');

  var devConf = {
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
          '-Pdevelop'
        ]
      },
      autoBuild('camunda-bpm-sdk-js', verbose, stack),
      // // autoBuild('camunda-commons-ui', verbose, stack),
      autoBuild('camunda-tasklist-ui', verbose, stack),
      autoBuild('camunda-cockpit-ui', verbose, stack),
      autoBuild('camunda-admin-ui', verbose, stack)
    ]
  };

  grunt.initConfig({
    parallel: {
      develop: devConf
    }
  });

  grunt.registerTask('default', ['parallel:develop']);
};
