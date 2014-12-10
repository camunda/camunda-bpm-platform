module.exports = function(grunt) {
  'use strict';

  require('load-grunt-tasks')(grunt);

  var pkg = require('./package.json');

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;

  grunt.initConfig({
    pkg:              pkg,

    requirejs:        require('./grunt/config/requirejs')(config),

    clean:            require('./grunt/config/clean')(config),

    watch:            require('./grunt/config/watch')(config)
  });

  grunt.registerTask('build', function(mode) {

    grunt.config.data.mode = mode || 'prod';

    grunt.task.run(['clean', 'requirejs']);
  });

  grunt.registerTask('auto-build', [
    'build:dev',
    'watch'
  ]);

  grunt.registerTask('default', ['build']);
};
