module.exports = function(grunt) {
  'use strict';

  require('load-grunt-tasks')(grunt);

  var pkg = require('./package.json');
  var protractorConfig = grunt.option('protractorConfig') || 'src/test/js/e2e/ci.conf.js';

  var config = pkg.gruntConfig || {};

  config.grunt = grunt;
  config.pkg = pkg;
  config.protractorConfig = protractorConfig;

  grunt.initConfig({
    pkg:              pkg,

    requirejs:        require('./grunt/config/requirejs')(config),

    clean:            require('./grunt/config/clean')(config),

    watch:            require('./grunt/config/watch')(config),

    protractor:       require('./grunt/config/protractor')(config)
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

  grunt.registerTask('test-e2e', ['build', 'protractor:e2e']);
};
