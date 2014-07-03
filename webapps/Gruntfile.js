/* jshint node: true */
'use strict';

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  var pkg = require('./package.json');

  var config = pkg.gruntConfig || {};

  config.connectPort = parseInt(process.env.CONNECT_PORT || config.connectPort, 10) || 7070;
  config.livereloadPort = (parseInt(process.env.LIVERELOAD_PORT, 10) || config.connectPort + 1);

  config.grunt = grunt;
  config.pkg = pkg;

  grunt.initConfig({
    buildTarget:      grunt.option('target'),

    pkg:              pkg,

    bower:            require('./grunt/config/bower')(config),

    jasmine_node:     require('./grunt/config/jasmine_node')(config),

    karma:            require('./grunt/config/karma')(config),

    protractor:       require('./grunt/config/protractor')(config),

    seleniuminstall:  require('./grunt/config/seleniuminstall')(config),

    requirejs:        require('./grunt/config/requirejs')(config),

    less:             require('./grunt/config/less')(config),

    copy:             require('./grunt/config/copy')(config),

    watch:            require('./grunt/config/watch')(config),

    connect:          require('./grunt/config/connect')(config),

    jsdoc:            require('./grunt/config/jsdoc')(config),

    jshint:           require('./grunt/config/jshint')(config),

    changelog:        require('./grunt/config/changelog')(config),

    clean:            ['doc', 'dist', '.tmp']
  });

  grunt.registerTask('build', [
    'clean',
    'jshint',
    'jsdoc',
    'bower',
    'copy',
    'less',
    'requirejs'
  ]);

  grunt.registerTask('auto-build', [
    'build',
    'connect',
    'watch'
  ]);

  grunt.registerTask('postinstall', ['seleniuminstall']);

  grunt.registerTask('prepublish', ['build', 'changelog']);

  grunt.registerTask('default', ['build']);
};
