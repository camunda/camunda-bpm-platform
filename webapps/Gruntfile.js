/* global require: false, module: false */
'use strict';

/**
  This file is used to configure the [grunt](http://gruntjs.com/) tasks
  aimed to generate the web frontend of the camunda BPM platform.
  @author Valentin Vago <valentin.vago@camunda.com>
  @author Nico Rehwaldt  <nico.rehwaldt@camunda.com>
 */

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);
  var commons = require('camunda-commons-ui');

  var pkg = require('./package.json');

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;

  grunt.initConfig({
    // the default value should be the "dev" destination
    // this is not exactly the best solution, but otherwise, when tasks are "rerunned"
    // with the watch tasks, the value get lost...
    // so, it's fine for the "dist" build mode
    // who will override the value but only run once
    buildTarget:      grunt.option('target') || config.devTarget,

    pkg:              pkg,

    requirejs:        require('./grunt/config/requirejs')(config),

    less:             require('camunda-commons-ui/grunt/config/less')(config),

    copy:             require('./grunt/config/copy')(config),

    watch:            require('./grunt/config/watch')(config),

    jshint:           require('camunda-commons-ui/grunt/config/jshint')(config),

    changelog:        require('camunda-commons-ui/grunt/config/changelog')(config),

    clean:            require('camunda-commons-ui/grunt/config/clean')(config)
  });

  grunt.registerTask('build', commons.builder(grunt));


  grunt.registerTask('auto-build', [
    'build:dev',
    'watch'
  ]);


  grunt.registerTask('prepublish', ['build', 'changelog']);

  grunt.registerTask('default', ['build']);
};
