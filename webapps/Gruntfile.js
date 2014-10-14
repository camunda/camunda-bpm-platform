/* global require: false */
'use strict';

/**
  This file is used to configure the [grunt](http://gruntjs.com/) tasks
  aimed to generate the web frontend of the camunda BPM platform.
  @author Valentin Vago <valentin.vago@camunda.com>
  @author Nico Rehwaldt  <nico.rehwaldt@camunda.com>
 */

module.exports = function(grunt) {
  require('load-grunt-tasks')(grunt);

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

    bower:            require('camunda-commons-ui/grunt/config/bower')(config),

    jasmine_node:     require('camunda-commons-ui/grunt/config/jasmine_node')(config),

    karma:            require('camunda-commons-ui/grunt/config/karma')(config),

    requirejs:        require('./grunt/config/requirejs')(config),

    less:             require('camunda-commons-ui/grunt/config/less')(config),

    copy:             require('./grunt/config/copy')(config),

    watch:            require('./grunt/config/watch')(config),

    connect:          require('camunda-commons-ui/grunt/config/connect')(config),

    jsdoc:            require('camunda-commons-ui/grunt/config/jsdoc')(config),

    jshint:           require('camunda-commons-ui/grunt/config/jshint')(config),

    changelog:        require('camunda-commons-ui/grunt/config/changelog')(config),

    clean:            require('camunda-commons-ui/grunt/config/clean')(config)
  });


  grunt.registerTask('custom-copy', function() {
    var smthRandom = 'ad'+ (new Date()).getTime();
    var tasks = [
      'copy:'+ smthRandom,
      '-custom-copy:'+ smthRandom
    ];

    grunt.config.data.copy[smthRandom] = {
      options: {
        process: function(content, srcpath) {
          var processing = (grunt.config.get('buildTarget') !== 'dist' ? 'dev' : 'dist') +'FileProcessing';

          return (require('./grunt/config/copy')[processing]).call(grunt, content, srcpath);
        }
      },
      files: [
        {
          expand: true,
          cwd: 'node_modules/camunda-commons-ui/',
          src: [
            'lib/**/*.*',
            'lib/*.*'
          ],
          dest: '<%= buildTarget %>/assets/vendor/camunda-commons-ui/',
        },
        {
          expand: true,
          cwd: '<%= pkg.gruntConfig.clientDir %>/scripts/',
          src: [
            '**/*.*',
            '*.*'
          ],
          dest: '<%= buildTarget %>/',
        }
      ]
    };

    grunt.task.run(tasks);
  });

  grunt.registerTask('-custom-copy', function(id) {
    for (var t in grunt.config.data) {
      for (var tt in grunt.config.data[t]) {
        if (tt.slice(0 - id.length) === id) {
          delete grunt.config.data[t][tt];
        }
      }
    }
  });



  grunt.registerTask('build', function(mode) {
    mode = mode || 'prod';

    grunt.config.data.buildTarget = (mode === 'prod' ? config.prodTarget : config.devTarget);
    grunt.log.subhead('Will build the "'+ pkg.name +'" project in "'+ mode +'" mode and place it in "'+ grunt.config('buildTarget') +'"');
    if (mode === 'dev') {
      grunt.log.writeln('Will serve on port "'+
        config.connectPort +
        '" and liverreload available on port "'+
        config.livereloadPort +
        '"');
    }

    var tasks = [
      'clean',
//       'jshint',
//       'jsdoc',
      'bower',
      'copy',
      'less',
      // NOTE: the requirejs task is actually
      // overriden using "grunt.renameTask".
      // In a world of unicorns and rainbows,
      // the normal requirejs should of course be used.
      'requirejs'
    ];

    grunt.task.run(tasks);
  });


  grunt.renameTask('custom-copy', 'requirejs');


  grunt.registerTask('auto-build', [
    'build:dev',
    // 'connect',
    'watch'
  ]);


  grunt.registerTask('prepublish', ['build', 'changelog']);

  grunt.registerTask('default', ['build']);
};
