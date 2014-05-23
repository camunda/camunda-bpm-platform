/* jshint node: true */
'use strict';

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);
  require('time-grunt')(grunt);

  var pkg = require('./package.json');

  var config = pkg.gruntConfig || {
    connectPort:    7070
  };

  config.connectPort = parseInt(process.env.CONNECT_PORT || config.connectPort);
  config.livereloadPort = parseInt(process.env.LR_PORT || (config.connectPort + 1));

  config.grunt = grunt;
  config.pkg = pkg;

  grunt.initConfig({
    pkg:              pkg,

    bower:            require('./grunt/config/bower')(config),

    // sadly, this is not ready yet.
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

  grunt.registerTask('build', function(target) {
    target = target || 'dist';

    var tasks = [
      'clean',
      'jshint',
      'jsdoc',
      'bower',
      'copy',
      'less',
      'requirejs'
    ];

    grunt.task.run(tasks);
  });

  grunt.registerTask('serve', [
    'build:dev',
    'connect',
    'watch'
  ]);

  grunt.registerTask('postinstall', ['seleniuminstall']);

  grunt.registerTask('prepublish', ['build', 'changelog']);

  grunt.registerTask('default', ['build']);
};
